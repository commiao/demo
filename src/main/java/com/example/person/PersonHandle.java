package com.example.person;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.example.ExcelTool;
import com.example.demo.entity.exportDTO.ExportCompanyData;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.person.listener.PersonListener;

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

    private static Map<String, List<ExcelPerson>> buildPersonList(List<ExcelPerson> list) {
        Map<String, List<ExcelPerson>> personMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPerson::getName));
        Iterator<Map.Entry<String, List<ExcelPerson>>> it = personMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<ExcelPerson>> entry = it.next();
            if (!checkCity(entry.getValue())) {
                it.remove();//使用迭代器的remove()方法删除元素
            }
        }
        return personMap;
    }

    /**
     * 校验发明人个人所在城市是否超过了1个
     * 超过2个说明该发明人是潜在的迁移数据
     * 此处不处理重名问题
     */
    private static boolean checkCity(List<ExcelPerson> list) {
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

    private static void write(String excelWritePath, List<ExcelPerson> list) {
        ExcelWriter excelWriter = EasyExcel.write(excelWritePath).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "inventor").head(ExcelPerson.class).build();
        excelWriter.write(list, writeSheet);
        excelWriter.finish();
    }

    public static void main(String[] args) {
//        String excelFilePath = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        String excelFilePath = "F:\\excel\\210908\\en_rd_person.xlsx";
        PersonListener listen = build(excelFilePath);
        List<ExcelPerson> list = listen.getPersonList();

        Map<String, List<ExcelPerson>> map = buildPersonList(list);

        List<ExcelPerson> tList = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPerson>> entry : map.entrySet()) {
            List<ExcelPerson> l = entry.getValue().parallelStream().sorted(Comparator.comparing(ExcelPerson::getYear).thenComparing(ExcelPerson::getSymbol)).collect(Collectors.toList());
            tList.addAll(l);
        }
        // excel输出地址
        String excelWritePath = "F:\\excel\\210908\\inventor.xlsx";
        write(excelWritePath, tList);

//        list.stream().filter(excelPerson ->
//                excelPerson.getSymbol().equals("000012")
//                        && excelPerson.getYear().equals(2013)
//                        && excelPerson.getName().equals("李大平")
//        ).forEach(
//                excelPerson -> System.out.println(JSONObject.toJSON(excelPerson))
//        );
    }
}
