package com.example.person;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.person.listener.PersonPatentListener;
import lombok.Builder;
import lombok.Getter;

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

    private static Map<String, List<ExcelPatent>> buildPersonList(List<ExcelPatent> list) {
        Map<String, List<ExcelPatent>> personMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPatent::getName));
        Iterator<Map.Entry<String, List<ExcelPatent>>> it = personMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<ExcelPatent>> entry = it.next();
            if (!checkMoreCity(entry.getValue()) || checkSameName(entry.getValue())) {
                it.remove();//使用迭代器的remove()方法删除元素
            }
        }
        return personMap;
    }

    @Builder
    @Getter
    public class UserSymbolYearDTO {
        String symbol;
        Integer minYear;
        Integer maxYear;
    }

    private void signUserCode(List<ExcelPatent> personList) {
        Map<String, List<ExcelPatent>> symbolMap = personList.parallelStream().collect(Collectors.groupingBy(ExcelPatent::getSymbol));
        List<UserSymbolYearDTO> l = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : symbolMap.entrySet()) {
            List<ExcelPatent> list = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            UserSymbolYearDTO dto = UserSymbolYearDTO.builder()
                    .symbol(entry.getKey()).minYear(list.get(0).getYear()).maxYear(list.get(list.size() - 1).getYear())
                    .build();
            l.add(dto);
        }
        l = l.stream().sorted(Comparator.comparing(UserSymbolYearDTO::getMinYear)).collect(Collectors.toList());
        for (int i = 0; i < l.size(); i++) {
            UserSymbolYearDTO dto = l.get(i);
            // 排除一个公司的数据
            List<UserSymbolYearDTO> temp = l.stream().filter(entry -> !entry.getSymbol().equals(dto.getSymbol())).collect(Collectors.toList());
            boolean isOne = false;
            if (temp.size() > 0) {
                UserSymbolYearDTO current = null;
                for (UserSymbolYearDTO otherSymbol : temp) {
                    // 最大年份 > 其他公司的最小年份/最大年份 > 最小年份  不是同一个人
                    // 最大年份 = 其他公司最小年份  专利不同   不是同一人
                    // 最大年份 = 其他公司最小年份  @TODO(专利相同)   是同一人
                    if (dto.getMaxYear() == otherSymbol.getMinYear()) {
                        current = otherSymbol;
                        isOne = true;
                    }
                    // 最大年份 < 其他公司最小年份   是同一人
                    if (dto.getMaxYear() < otherSymbol.getMinYear()) {
                        current = otherSymbol;
                        isOne = true;
                    }
                }
                // 是同一人的时候
                if (isOne) {
                    String userCode = createUserCode();
                    symbolMap.get(dto.getSymbol()).forEach(item -> item.setUserCode(userCode));
                    symbolMap.get(current.getSymbol()).forEach(item -> item.setUserCode(userCode));
                }
            }
        }

    }

    private static int number = 10000000;

    private static String createUserCode() {
        number++;
        return "T" + number;
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
     * 判断是否迁移过
     * 校验发明人个人所在城市是否超过了1个
     * 超过2个说明该发明人是潜在的迁移数据
     * 此处不处理重名问题
     */
    private static boolean checkMoreCity(List<ExcelPatent> list) {
        String city = null;
        boolean isMoreCity = false;
        for (ExcelPatent person : list) {
            if (city == null) {
                city = person.getCityCodeMain();
                continue;
            }
            if (!city.equals(person.getCityCodeMain())) {
                isMoreCity = true;
                break;
            }
        }
        return isMoreCity;
    }

    public static void main(String[] args) {
        String excelFilePath = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
//        String excelFilePath = "F:\\excel\\210908\\en_rd_person.xlsx";
        PersonPatentListener listen = build(excelFilePath);
        List<ExcelPatent> list = listen.getPersonList();

        // 去除无效数据
        Map<String, List<ExcelPatent>> map = buildPersonList(list);
        List<ExcelPatent> tList = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : map.entrySet()) {
//            List<ExcelPerson> l = entry.getValue().parallelStream().sorted(Comparator.comparing(ExcelPerson::getYear).thenComparing(ExcelPerson::getSymbol)).collect(Collectors.toList());

            List<ExcelPatent> l = entry.getValue().parallelStream()
                    .sorted(Comparator.comparing(ExcelPatent::getSymbol).thenComparing(ExcelPatent::getYear))
                    .collect(Collectors.toList());
            tList.addAll(l);
        }

        // excel输出地址
        String excelWritePath = "F:\\excel\\210908\\inventor_symbol.xlsx";
        ExcelTool.write(excelWritePath, tList, ExcelPatent.class);

//        list.stream().filter(excelPerson ->
//                excelPerson.getSymbol().equals("000012")
//                        && excelPerson.getYear().equals(2013)
//                        && excelPerson.getName().equals("李大平")
//        ).forEach(
//                excelPerson -> System.out.println(JSONObject.toJSON(excelPerson))
//        );
    }

    private static String buildPersonCityChangeStr(ExcelPatent person) {
        StringBuffer sb = new StringBuffer();
        sb.append(person.getYear()).append("_")
                .append(person.getName()).append("_")
                .append(person.getSymbol()).append("_")
                .append(person.getCityCodeMain()).append("_")
                .append(person.getCity());
        return sb.toString();
    }
}
