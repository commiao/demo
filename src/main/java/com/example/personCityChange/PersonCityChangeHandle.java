package com.example.personCityChange;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.personCity.entity.CityChangeDTO;
import com.example.personCity.listener.PersonListener;
import com.example.personCityChange.entity.ExcelPersonCityChange;
import com.example.personCityChange.listener.PersonCityChangeListener;

import java.util.List;

public class PersonCityChangeHandle {
    private static PersonCityChangeListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonCityChangeListener listener = new PersonCityChangeListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(PersonCityChangeListener.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }


    public static void main(String[] args) {
        String excelFilePath = "F:\\share with me\\public\\小井\\jing_处理好的数据\\210917\\city_change_pm.xlsx";
        PersonCityChangeListener listener = build(excelFilePath);
        List<ExcelPersonCityChange> cityChangeList = listener.getPersonCityChangeList();
        System.out.println(cityChangeList.size());
//        String excelWritePath = "F:\\excel\\210908\\person_city_change.xlsx";
//        ExcelTool.write(excelWritePath, cityChangeList, CityChangeDTO.class);

    }

}
