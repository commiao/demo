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
import com.example.personCityChange.PersonCityChangeHandle;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发明人与专利逻辑处理
 */
public class PersonPatentHandle {

    private static PersonPatentListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonPatentListener listener = new PersonPatentListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPatent.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    public static List<ExcelPatent> getAllPersonPatent(String en_rd_person_excel_path) {
        PersonPatentListener listen = build(en_rd_person_excel_path);
        return listen.getPersonList();
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
        List<ExcelPatent> allPersonPatentList = getAllPersonPatent(en_rd_person_excel_path);
        // 去除无效数据
        CountPersonNameDTO dto = PersonCityChangeHandle.buildPersonList(allPersonPatentList);

        // excel输出地址
        String excelWritePath_no = "F:\\excel\\211024\\no_move.xlsx";
        ExcelTool.write(excelWritePath_no, 0, "no_move", dto.getNoMoveList(), ExcelPatent.class);

        // excel输出地址
        String excelWritePath_yes = "F:\\excel\\211024\\yes_move.xlsx";
        ExcelTool.write(excelWritePath_yes, 0, "yes_move", dto.getYesMoveList(), ExcelPatent.class);


//        // 汇总全部城市的数据
//        Map<String, List<ExcelPatent>> map_no = dto.getNoMoveList().stream().collect(Collectors.groupingBy(ExcelPatent::getCityCodeMain));
//        List<ExcelPatent> count_list_no = new ArrayList<>();
//        for (Map.Entry<String, List<ExcelPatent>> city_entry : map_no.entrySet()) {
//            count_list_no.add(countPersonPatent(city_entry.getValue()));
//        }
//        // excel输出地址
//        String excelWritePath_countParentForCity_no = "F:\\excel\\211024\\count_patent_for_city_no.xlsx";
//        ExcelTool.write(excelWritePath_countParentForCity_no, count_list_no, ExcelPatent.class);
//
//
//        List<ExcelPerson> list = new ArrayList<>(dto.getYesMoveList());
//        List<ExcelCity> personCityList = PersonCityHandle.buildCityTypeList(list);
//        Map<String, List<ExcelPatent>> user_map = dto.getYesMoveList().stream().collect(Collectors.groupingBy(ExcelPerson::getUserCode));
//        Map<String, List<ExcelCity>> city_map = personCityList.stream().collect(Collectors.groupingBy(ExcelPerson::getCityCodeMain));
//        List<ExcelPatent> result = new ArrayList<>();
//        for (Map.Entry<String, List<ExcelPatent>> user : user_map.entrySet()) {
//            List<ExcelCity> cityList = city_map.get(user.getValue());
//            List<ExcelPatent> patentList = user.getValue();
//            result.addAll(countPersonPatentByCity(patentList, cityList));
//        }
//        // 汇总全部城市的数据
//        Map<String, List<ExcelPatent>> map_yes = result.stream().collect(Collectors.groupingBy(ExcelPatent::getCityCodeMain));
//        List<ExcelPatent> count_list_yes = new ArrayList<>();
//        for (Map.Entry<String, List<ExcelPatent>> city_entry : map_yes.entrySet()) {
//            count_list_yes.add(countPersonPatent(city_entry.getValue()));
//        }
//        // excel输出地址
//        String excelWritePath_countParentForCity_yes = "F:\\excel\\211024\\count_patent_for_city_yes.xlsx";
//        ExcelTool.write(excelWritePath_countParentForCity_yes, count_list_yes, ExcelPatent.class);
//
    }
}
