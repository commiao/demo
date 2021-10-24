package com.example.personCityChange;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.ExcelWriteBean;
import com.example.personCityChange.entity.CityMoveCountDTO;
import com.example.personCityChange.entity.ExcelPersonCityChange;
import com.example.personCityChange.listener.PersonCityChangeListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 以发明人为视角
 * 处理发明人城市迁移逻辑
 */
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
        String excelFilePath = "F:\\excel\\210908\\ ";
        PersonCityChangeListener listener = build(excelFilePath);
        List<ExcelPersonCityChange> cityChangeList = listener.getPersonCityChangeList();
        System.out.println(cityChangeList.size());
        BigDecimal pm50 = new BigDecimal("35");
        Map<Integer, List<ExcelPersonCityChange>> yearMap = cityChangeList.parallelStream().collect(Collectors.groupingBy(ExcelPersonCityChange::getYear));
        List<CityMoveCountDTO> dtoList = new ArrayList<>();
        List<ExcelPersonCityChange> goodToBadList = new ArrayList<>();
        List<ExcelPersonCityChange> badToGoodList = new ArrayList<>();
        List<ExcelPersonCityChange> otherList = new ArrayList<>();
        for (int i = 2011; i < 2019; i++) {
            List<ExcelPersonCityChange> tempList = yearMap.get(i);
            int goodToBadCount = 0;
            int badToGoodCount = 0;
            for (ExcelPersonCityChange item : tempList) {
                if (pm50.compareTo(item.getLastPmScore()) > 0 && pm50.compareTo(item.getNowPmScore()) <= 0) {
                    // 优良 --》 差
                    goodToBadList.add(item);
                    goodToBadCount++;
                } else if (pm50.compareTo(item.getLastPmScore()) <= 0 && pm50.compareTo(item.getNowPmScore()) > 0) {
                    // 差 --》 优良
                    badToGoodList.add(item);
                    badToGoodCount++;
                } else {
                    // 其他
                    otherList.add(item);
                }
            }
            dtoList.add(CityMoveCountDTO.builder()
                    .year(i).badToGoodCount(badToGoodCount).goodToBadCount(goodToBadCount)
                    .build());
        }
        List<ExcelWriteBean> beanList = new ArrayList<>();
        ExcelWriteBean count_bean = ExcelTool.buildExcelWriteBean(0, "city_move_count", dtoList, CityMoveCountDTO.class);
        beanList.add(count_bean);
        ExcelWriteBean bad_bean = ExcelTool.buildExcelWriteBean(1, "to_bad", goodToBadList, ExcelPersonCityChange.class);
        beanList.add(bad_bean);
        ExcelWriteBean good_bean = ExcelTool.buildExcelWriteBean(2, "to_good", badToGoodList, ExcelPersonCityChange.class);
        beanList.add(good_bean);
        ExcelWriteBean other_bean = ExcelTool.buildExcelWriteBean(3, "to_other", otherList, ExcelPersonCityChange.class);
        beanList.add(other_bean);
        String excelWritePath = "F:\\excel\\210908\\city_move_count.xlsx";
        ExcelTool.write(excelWritePath, beanList);

    }

}
