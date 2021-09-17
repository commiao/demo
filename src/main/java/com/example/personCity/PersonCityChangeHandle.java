package com.example.personCity;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.personCity.entity.CityChangeDTO;
import com.example.personCity.listener.PersonListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonCityChangeHandle {

    private static PersonListener build(String excelFilePath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelFilePath);
        PersonListener listener = new PersonListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ExcelPerson.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    /**
     * 收集构建每个发明家在所属城市的迁入迁出状况
     *
     * @param list
     */
    private static List<CityChangeDTO> findAllCityChangeList(List<ExcelPerson> list) {
        // 获取每个发明家的数据集合
        Map<String, List<ExcelPerson>> userMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPerson::getUserCode));
        List<CityChangeDTO> userCityChangeListAll = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPerson>> entry : userMap.entrySet()) {
            // 按时间排序，第一条为迁入，变换城市时，增加一条迁入（新城市）和一条迁出（旧城市）
            List<ExcelPerson> userCityList = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            List<CityChangeDTO> userCityChangeList = getUserCityChange(userCityList);
            userCityChangeListAll.addAll(userCityChangeList);
        }
        return userCityChangeListAll;
    }

    /**
     * 判断并标记一个发明家在所属城市的迁入迁出情况
     * 1、只有1条记录  标记为迁入
     * 2、变换城市时，该年新增一条迁出旧城市，一条迁入新城市
     *
     * @param userCityList
     */
    private static List<CityChangeDTO> getUserCityChange(List<ExcelPerson> userCityList) {
        List<CityChangeDTO> changeTypeList = new ArrayList<>();
        ExcelPerson current = null;
        for (int i = 0; i < userCityList.size(); i++) {
            ExcelPerson city = userCityList.get(i);
            if (i == 0) {
                current = city;
            } else if (!current.getCityCodeMain().equals(city.getCityCodeMain())) {
                CityChangeDTO changeDTO = CityChangeDTO.builder()
                        .year(city.getYear()).name(city.getName()).userCode(city.getUserCode())
                        .build();
                // 记录发明家迁入迁出数据
                changeDTO.setLastSymbol(current.getSymbol());
                changeDTO.setLastCity(current.getCity());
                changeDTO.setLastCityCode(current.getCityCodeMain());
                changeDTO.setNowSymbol(city.getSymbol());
                changeDTO.setNowCity(city.getCity());
                changeDTO.setNowCityCode(city.getCityCodeMain());
                changeTypeList.add(changeDTO);
                current = city;
            }
        }
        return changeTypeList;
    }


    public static void main(String[] args) {
        String excelFilePath = "F:\\excel\\210908\\yes_move.xlsx";
        PersonListener listener = build(excelFilePath);
        List<CityChangeDTO> cityChangeList = findAllCityChangeList(listener.getPersonList());

        String excelWritePath = "F:\\excel\\210908\\person_city_change.xlsx";
        ExcelTool.write(excelWritePath, cityChangeList, CityChangeDTO.class);

    }


}
