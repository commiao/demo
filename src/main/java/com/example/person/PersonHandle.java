package com.example.person;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.ExcelTool;
import com.example.person.entity.dto.PersonCityChangeDTO;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.person.listener.PersonListener;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PersonHandle {

    private static PersonListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonListener listener = new PersonListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPerson.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    // 1、发明人 年份  城市
    // 2、发明人-》所在城市超过2个的
    // 3、发明人去重-》同一年份，不同城市，发明类型不同
    // 4、发明人离开城市年份、进入城市年份
    // 5、汇总进入/离开同一城市的发明人集合，区分年份
    // 6、根据城市 筛选出 从高质量到低质量/从低质量到高质量的数据集合

    private static Map<String, List<ExcelPerson>> buildPersonList(List<ExcelPerson> list) {
        Map<String, List<ExcelPerson>> personMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPerson::getName));
        Iterator<Map.Entry<String, List<ExcelPerson>>> it = personMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<ExcelPerson>> entry = it.next();
            if (!checkMoreCity(entry.getValue()) || checkSameName(entry.getValue())) {
                it.remove();//使用迭代器的remove()方法删除元素
            }
        }
        return personMap;
    }

    /**
     * 去除重名
     *
     * @TODO
     */
    private static boolean checkSameName(List<ExcelPerson> list) {
        // 把个人数据集合  按照年份分组
        Map<Integer, List<ExcelPerson>> result = list.parallelStream().collect(Collectors.groupingBy(ExcelPerson::getYear));
        // 同一年份 出现在两个以上的城市是潜在重名数据
        for (Map.Entry<Integer, List<ExcelPerson>> entry : result.entrySet()) {
            Map<String, List<ExcelPerson>> cityMap = entry.getValue().stream().collect(Collectors.groupingBy(ExcelPerson::getCityCodeMain));
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
    private boolean isSamePatent(ExcelPerson data1, ExcelPerson data2) {
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
    private static boolean checkMoreCity(List<ExcelPerson> list) {
        String city = null;
        boolean isMoreCity = false;
        for (ExcelPerson person : list) {
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

    /**
     * 按时间、公司排序，并标记迁入迁出
     *
     * @param tList
     * @return
     */
    private static List<PersonCityChangeDTO> buildCityChangeList(List<ExcelPerson> tList) {
        // 名字+公司分组
        LinkedHashMap<String, List<ExcelPerson>> nameSymbolMap = new LinkedHashMap<>();
        String key = null;
        String currentKey = null;
        for (ExcelPerson person : tList) {
            currentKey = person.getName() + "_" + person.getSymbol();
            if (currentKey.equals(key)) {
                nameSymbolMap.get(key).add(person);
            } else {
                key = currentKey;
                nameSymbolMap.put(key, new ArrayList<ExcelPerson>() {{
                    add(person);
                }});
            }
        }

        String name = null;
        String currentName = null;
        List<PersonCityChangeDTO> cityChangeDTOList = new ArrayList<>();
        List<String> yearSymbolList = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPerson>> entry : nameSymbolMap.entrySet()) {
            // 按年份排序，获取第一个年份
            List<ExcelPerson> temp = entry.getValue().parallelStream()
                    .sorted(Comparator.comparing(ExcelPerson::getYear))
                    .collect(Collectors.toList());
            currentName = temp.get(0).getName();
            String personSymbolKey = null;
            if (!currentName.equals(name)) {
                name = currentName;
                Collections.sort(yearSymbolList, (s1, s2) -> s1.compareTo(s2));
                // 个人一个公司，第一个年份标记为迁入  最后一个年份标记为迁出
                for (String str : yearSymbolList) {
                    String[] arr = str.split("_");
                    personSymbolKey = arr[1] + "_" + arr[2];
                    List<ExcelPerson> personSymbolList = nameSymbolMap.get(personSymbolKey);
                    for (int i = 0; i < personSymbolList.size(); i++) {
                        PersonCityChangeDTO dto = new PersonCityChangeDTO();
                        BeanUtils.copyProperties(personSymbolList.get(i), dto);
                        if (i == 0) {
                            dto.setChangType("迁入");
                        }
                        if (i == personSymbolList.size() - 1) {
                            if (personSymbolList.size() == 1) {
                                dto.setChangType("迁入_迁出");
                            } else {
                                dto.setChangType("迁出");
                            }
                        }
                        cityChangeDTOList.add(dto);
                    }
                }
                yearSymbolList = new ArrayList<>();
            }
            yearSymbolList.add(buildPersonCityChangeStr(temp.get(0)));
        }
        return cityChangeDTOList;
    }

    private static <T> void write(String excelWritePath, List<T> list, Class<T> clazz) {
        ExcelWriter excelWriter = EasyExcel.write(excelWritePath).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "inventor").head(clazz).build();
        excelWriter.write(list, writeSheet);
        excelWriter.finish();
    }

    public static void main(String[] args) {
        String excelFilePath = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
//        String excelFilePath = "F:\\excel\\210908\\en_rd_person.xlsx";
        PersonListener listen = build(excelFilePath);
        List<ExcelPerson> list = listen.getPersonList();

        Map<String, List<ExcelPerson>> map = buildPersonList(list);

        // 去除无效数据
        List<ExcelPerson> tList = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPerson>> entry : map.entrySet()) {
//            List<ExcelPerson> l = entry.getValue().parallelStream().sorted(Comparator.comparing(ExcelPerson::getYear).thenComparing(ExcelPerson::getSymbol)).collect(Collectors.toList());

            List<ExcelPerson> l = entry.getValue().parallelStream().sorted(Comparator.comparing(ExcelPerson::getSymbol).thenComparing(ExcelPerson::getYear))
                    .collect(Collectors.toList());
            tList.addAll(l);
        }

        // excel输出地址
//        String excelWritePath = "F:\\excel\\210908\\inventor_symbol.xlsx";
//        write(excelWritePath, tList, ExcelPerson.class);

        // 获取人员迁入迁出记录
        List<PersonCityChangeDTO> cityChangeDTOList = buildCityChangeList(tList);
        // excel输出地址
        String excelWritePath = "F:\\excel\\210908\\inventor_city_change.xlsx";
        write(excelWritePath, cityChangeDTOList, PersonCityChangeDTO.class);

//        list.stream().filter(excelPerson ->
//                excelPerson.getSymbol().equals("000012")
//                        && excelPerson.getYear().equals(2013)
//                        && excelPerson.getName().equals("李大平")
//        ).forEach(
//                excelPerson -> System.out.println(JSONObject.toJSON(excelPerson))
//        );
    }

    private static String buildPersonCityChangeStr(ExcelPerson person) {
        StringBuffer sb = new StringBuffer();
        sb.append(person.getYear()).append("_")
                .append(person.getName()).append("_")
                .append(person.getSymbol()).append("_")
                .append(person.getCityCodeMain()).append("_")
                .append(person.getCity());
        return sb.toString();
    }
}
