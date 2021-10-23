package com.example.person;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.person.entity.dto.CountPersonNameDTO;
import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.person.listener.PersonPatentListener;
import com.example.personCity.PersonCityHandle;
import com.example.personCity.entity.ExcelCity;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PersonPatentHandle {

    private static PersonPatentListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonPatentListener listener = new PersonPatentListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPatent.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    // 1、发明人 年份  城市
    // 2、发明人-》所在城市超过2个的
    // 3、发明人去重-》同一年份，不同城市，发明类型不同
    // 4、发明人离开城市年份、进入城市年份
    // 5、汇总进入/离开同一城市的发明人集合，区分年份
    // 6、根据城市 筛选出 从高质量到低质量/从低质量到高质量的数据集合

    private static CountPersonNameDTO buildPersonList(List<ExcelPatent> list) {
        Map<String, List<ExcelPatent>> personMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPatent::getName));
//        Iterator<Map.Entry<String, List<ExcelPatent>>> it = personMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, List<ExcelPatent>> entry = it.next();
//            if (!checkMoreCity(entry.getValue()) || checkSameName(entry.getValue())) {
//                it.remove();//使用迭代器的remove()方法删除元素
//            }
//        }
        System.out.println("========================共" + list.size() + "条数据，" + personMap.size() + "个人名");
        Map<String, List<ExcelPatent>> noMove = new HashMap<>();
        Map<String, List<ExcelPatent>> yesMove = new HashMap<>();
        Map<String, List<ExcelPatent>> todoData = new HashMap<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : personMap.entrySet()) {
            List<ExcelPatent> userList = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            Map<String, List<ExcelPatent>> symbolMap = userList.stream().collect(Collectors.groupingBy(ExcelPerson::getSymbol));
            if (symbolMap.size() == 1) {
                // 一个人名只在一个城市出现过
                setNoMoveUserCode(userList);
                noMove.put(userList.get(0).getName(), userList);
            } else {
                // 一个人名出现在多个城市
                if (checkIsSame(symbolMap)) {
                    // 1、时间没有交集  迁移了
                    setYesMoveUserCode(userList);
                    yesMove.put(userList.get(0).getName(), userList);
                } else {
                    // 2、时间有交集 @TODO 按重名剔除
                    setUserCode(userList, "******");
                    todoData.put(userList.get(0).getName(), userList);
                }
            }
        }
        System.out.println("========================未迁移人数" + noMove.size() + "个人，已迁移人数" + yesMove.size() + "个人，有问题人数" + todoData.size() + "个人");
        return CountPersonNameDTO.builder().noMove(noMove).yesMove(yesMove).todoData(todoData).build();
    }

    private static void setYesMoveUserCode(List<ExcelPatent> personList) {
        String userCode = createUserCode("Y");
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    private static void setNoMoveUserCode(List<ExcelPatent> personList) {
        String userCode = createUserCode("N");
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    private static void setUserCode(List<ExcelPatent> personList, String userCode) {
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    @Data
    public static class UserSymbolYearDTO {
        String symbol;
        Integer minYear;
        Integer maxYear;
    }

    private static boolean checkIsSame(Map<String, List<ExcelPatent>> symbolMap) {
        List<UserSymbolYearDTO> l = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : symbolMap.entrySet()) {
            List<ExcelPatent> list = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            UserSymbolYearDTO dto = new UserSymbolYearDTO();
            dto.setSymbol(entry.getKey());
            dto.setMinYear(list.get(0).getYear());
            dto.setMaxYear(list.get(list.size() - 1).getYear());
            l.add(dto);
        }
        l = l.stream().sorted(Comparator.comparing(UserSymbolYearDTO::getMinYear)).collect(Collectors.toList());
        boolean isOne = true;
        for (int i = 0; i < l.size(); i++) {
            UserSymbolYearDTO currDto = l.get(i);
            // 排除一个公司的数据
            List<UserSymbolYearDTO> temp = l.stream().filter(entry -> !entry.getSymbol().equals(currDto.getSymbol())).collect(Collectors.toList());
            if (temp.size() > 0) {
                for (UserSymbolYearDTO otherSymbol : temp) {
                    // 最大年份 > 其他公司的最小年份/最大年份 > 最小年份  不是同一个人
                    if ((currDto.getMaxYear() >= otherSymbol.getMinYear() && otherSymbol.getMinYear() >= currDto.getMinYear())) {
                        isOne = false;
                    }
                    if (currDto.getMaxYear() >= otherSymbol.getMaxYear() && otherSymbol.getMaxYear() >= currDto.getMinYear()) {
                        isOne = false;
                    }
                    // 最大年份 = 其他公司最小年份  专利不同   不是同一人
                    // 最大年份 = 其他公司最小年份  @TODO(专利相同)   是同一人
                    // 最大年份 < 其他公司最小年份   是同一人
                }
                l.remove(currDto);
                i--;
            }
        }
        return isOne;
    }

    private static int number = 10000000;

    private static String createUserCode(String type) {
        number++;
        return type + number;
    }

    /**
     * 去除重名
     *
     * @TODO
     */
    private static boolean checkSameName(List<ExcelPatent> list) {
        // 把个人数据集合  按照年份分组
        Map<Integer, List<ExcelPatent>> result = list.parallelStream().collect(Collectors.groupingBy(ExcelPatent::getYear));
        // 同一年份 出现在两个以上的城市是潜在重名数据
        for (Map.Entry<Integer, List<ExcelPatent>> entry : result.entrySet()) {
            Map<String, List<ExcelPatent>> cityMap = entry.getValue().stream().collect(Collectors.groupingBy(ExcelPatent::getCityCodeMain));
            if (cityMap.size() > 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param data1
     * @param data2
     * @return
     * @TODO
     */
    private boolean isSamePatent(ExcelPatent data1, ExcelPatent data2) {
        if (data1.getInventionSum().equals(data2.getInventionSum())
                && data1.getUtilityModelSum().equals(data2.getUtilityModelSum())
                && data1.getDesignSum().equals(data2.getDesignSum())) {
            return true;
        }
        return false;
    }

    public static CountPersonNameDTO getFilterMoveDTO(String en_rd_person_excel_path) {
        PersonPatentListener listen = build(en_rd_person_excel_path);
        List<ExcelPatent> list = listen.getPersonList();
        // 去除无效数据
        return buildPersonList(list);
    }

    /**
     * @param patentList 个人全部的发明专利数据集合
     * @param cityList   个人的城市迁移集合
     * @return
     */
    public static List<ExcelPatent> countPersonPatentByCity(List<ExcelPatent> patentList, List<ExcelCity> cityList) {
        List<ExcelPatent> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(cityList)) {
            list.add(countPersonPatent(patentList));
            return list;
        }
        patentList.sort(Comparator.comparing(ExcelPatent::getYear));
        cityList.sort(Comparator.comparing(ExcelCity::getYear));
        ExcelPatent city_patent = null;
        String currentCityCode = null;
        Integer beginYear = null;
        Integer endYear = null;
        for (ExcelCity city : cityList) {
            if (currentCityCode == null || "迁入".equals(city.getChangeType())) {
                city_patent = new ExcelPatent();
                city_patent.setUserCode(city.getUserCode());
                city_patent.setName(city.getName());
                city_patent.setCityCodeMain(city.getCityCodeMain());
                city_patent.setCity(city.getCity());
                beginYear = city.getYear();
                continue;
            }
            if ("迁出".equals(city.getChangeType())) {
                List<ExcelPatent> year_patent = new ArrayList<>();
                endYear = city.getYear();
                for (ExcelPatent patent : patentList) {
                    if (patent.getYear() < beginYear) {
                        continue;
                    }
                    if (patent.getYear() > endYear) {
                        break;
                    }
                    year_patent.add(patent);
                }
                list.add(countPersonPatent(year_patent));
            }
        }
        return list;
    }

    /**
     * 统计个人的发明专利汇总
     *
     * @param patentList
     * @return
     */
    private static ExcelPatent countPersonPatent(List<ExcelPatent> patentList) {
        ExcelPatent patent = new ExcelPatent();
        for (ExcelPatent excelPatent : patentList) {
            patent.setCity(excelPatent.getCity());
            patent.setCityCodeMain(excelPatent.getCityCodeMain());
            patent.setUserCode(excelPatent.getUserCode());
            patent.setName(excelPatent.getName());
            patent.setDesignSum(add(patent.getDesignSum(), excelPatent.getDesignSum()));
            patent.setInventionSum(add(patent.getInventionSum(), excelPatent.getInventionSum()));
            patent.setPatentSum(add(patent.getPatentSum(), excelPatent.getPatentSum()));
            patent.setUtilityModelSum(add(patent.getUtilityModelSum(), excelPatent.getUtilityModelSum()));
            patent.setQuoteSum(add(patent.getQuoteSum(), excelPatent.getQuoteSum()));
        }
        return patent;
    }

    private static int add(Integer last, Integer current) {
        last = last == null ? 0 : last;
        current = current == null ? 0 : current;
        return last + current;
    }

    public static void main(String[] args) {
//        String en_rd_person_excel_path = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        String en_rd_person_excel_path = "F:\\share with me\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        CountPersonNameDTO dto = getFilterMoveDTO(en_rd_person_excel_path);

        List<ExcelPatent> no_List = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : dto.getNoMove().entrySet()) {
            no_List.addAll(entry.getValue());
        }
        int i = no_List.size();
        System.out.println("######################未迁移数据" + i + "条");
        // excel输出地址
//        String excelWritePath_no = "F:\\excel\\211024\\no_move.xlsx";
//        no_List.parallelStream().sorted(Comparator.comparing(ExcelPatent::getName).thenComparing(ExcelPerson::getYear)).collect(Collectors.toList());
//        ExcelTool.write(excelWritePath_no, 0, "no_move", no_List, ExcelPatent.class);


        List<ExcelPatent> yes_List = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : dto.getYesMove().entrySet()) {
            yes_List.addAll(entry.getValue());
        }
        int j = yes_List.size();
        System.out.println("######################已迁移数据" + j + "条");
        // excel输出地址
//        String excelWritePath_yes = "F:\\excel\\211024\\yes_move.xlsx";
//        yes_List.parallelStream().sorted(Comparator.comparing(ExcelPatent::getName).thenComparing(ExcelPerson::getYear)).collect(Collectors.toList());
//        ExcelTool.write(excelWritePath_yes, 0, "yes_move", yes_List, ExcelPatent.class);


        // 汇总全部城市的数据
        Map<String, List<ExcelPatent>> map_no = no_List.stream().collect(Collectors.groupingBy(ExcelPatent::getCityCodeMain));
        List<ExcelPatent> count_list_no = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> city_entry : map_no.entrySet()) {
            count_list_no.add(countPersonPatent(city_entry.getValue()));
        }
        // excel输出地址
        String excelWritePath_countParentForCity_no = "F:\\excel\\211024\\count_patent_for_city_no.xlsx";
        ExcelTool.write(excelWritePath_countParentForCity_no, count_list_no, ExcelPatent.class);


        List<ExcelPerson> list = new ArrayList<>(yes_List);
        List<ExcelCity> personCityList = PersonCityHandle.buildCityTypeList(list);
        Map<String, List<ExcelPatent>> user_map = yes_List.stream().collect(Collectors.groupingBy(ExcelPerson::getUserCode));
        Map<String, List<ExcelCity>> city_map = personCityList.stream().collect(Collectors.groupingBy(ExcelPerson::getCityCodeMain));
        List<ExcelPatent> result = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> user : user_map.entrySet()) {
            List<ExcelCity> cityList = city_map.get(user.getValue());
            List<ExcelPatent> patentList = user.getValue();
            result.addAll(countPersonPatentByCity(patentList, cityList));
        }
        // 汇总全部城市的数据
        Map<String, List<ExcelPatent>> map_yes = result.stream().collect(Collectors.groupingBy(ExcelPatent::getCityCodeMain));
        List<ExcelPatent> count_list_yes = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> city_entry : map_yes.entrySet()) {
            count_list_yes.add(countPersonPatent(city_entry.getValue()));
        }
        // excel输出地址
        String excelWritePath_countParentForCity_yes = "F:\\excel\\211024\\count_patent_for_city_yes.xlsx";
        ExcelTool.write(excelWritePath_countParentForCity_yes, count_list_yes, ExcelPatent.class);

    }
}
