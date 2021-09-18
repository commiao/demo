package com.example.personCityChange;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.personCityChange.entity.CityMoveCountDTO;
import com.example.personCityChange.entity.ExcelPersonCityChange;
import com.example.personCityChange.listener.PersonCityChangeListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonCityChangeHandle {
    private static PersonCityChangeListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonCityChangeListener listener = new PersonCityChangeListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPersonCityChange.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }


    public static void main(String[] args) {
//        String excelFilePath = "F:\\share with me\\public\\小井\\jing_处理好的数据\\210917\\city_change_pm.xlsx";
        String excelFilePath = "F:\\excel\\210908\\city_change_pm.xlsx";
        PersonCityChangeListener listener = build(excelFilePath);
        List<ExcelPersonCityChange> cityChangeList = listener.getPersonCityChangeList();
        System.out.println(cityChangeList.size());
        BigDecimal pm50 = new BigDecimal("35");
        // 优良 --》 差
        Map<Integer, List<ExcelPersonCityChange>> goodToBadMap = cityChangeList.parallelStream().filter(item -> {
            return pm50.compareTo(item.getLastPmScore()) > 0 && pm50.compareTo(item.getNowPmScore()) <= 0;
        }).collect(Collectors.groupingBy(ExcelPersonCityChange::getYear));

        // 差 --》 优良
        Map<Integer, List<ExcelPersonCityChange>> badToGoodMap = cityChangeList.parallelStream().filter(item -> {
            return pm50.compareTo(item.getLastPmScore()) <= 0 && pm50.compareTo(item.getNowPmScore()) > 0;
        }).collect(Collectors.groupingBy(ExcelPersonCityChange::getYear));

        List<CityMoveCountDTO> dtoList = new ArrayList<>();
        for (int i = 2010; i < 2019; i++) {
            List<ExcelPersonCityChange> goodToBadList = goodToBadMap.get(i);
            List<ExcelPersonCityChange> badToGoodList = badToGoodMap.get(i);
            dtoList.add(CityMoveCountDTO.builder()
                    .year(i).badToGoodCount(badToGoodList == null ? 0 : badToGoodList.size()).goodToBadCount(goodToBadList == null ? 0 : goodToBadList.size())
                    .build());
        }

        String excelWritePath = "F:\\excel\\210908\\city_move_count.xlsx";
        ExcelTool.write(excelWritePath, dtoList, CityMoveCountDTO.class);

    }

}
