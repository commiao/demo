package com.example.person;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSONObject;
import com.example.ExcelTool;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.person.listener.PersonListener;

import java.util.List;

public class PersonHandle {

    private static PersonListener build() {
        ExcelReader excelReader = ExcelTool.getExcelReader("F:\\excel\\210908\\en_rd_person.xlsx");
        PersonListener listener = new PersonListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPerson.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    public static void main(String[] args) {
        PersonListener listen = build();
        List<ExcelPerson> list = listen.getPersonList();
        list.stream().filter(excelPerson ->
                excelPerson.getSymbol().equals("000012")
                        && excelPerson.getYear().equals(2013)
                        && excelPerson.getName().equals("李大平")
        ).forEach(
                excelPerson -> System.out.println(JSONObject.toJSON(excelPerson))
        );
    }
}
