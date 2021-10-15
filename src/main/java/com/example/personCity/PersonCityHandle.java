package com.example.personCity;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.person.PersonPatentHandle;
import com.example.person.entity.dto.CountPersonNameDTO;
import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.personCity.entity.ExcelCity;
import com.example.personCity.listener.PersonListener;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PersonCityHandle {

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
    public static List<ExcelCity> buildCityTypeList(List<ExcelPerson> list) {
        // 获取每个发明家的数据集合
        Map<String, List<ExcelPerson>> userMap = list.stream().collect(Collectors.groupingBy(ExcelPerson::getUserCode));
        List<ExcelCity> userCityChangeListAll = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPerson>> entry : userMap.entrySet()) {
            // 按时间排序，第一条为迁入，变换城市时，增加一条迁入（新城市）和一条迁出（旧城市）
            List<ExcelPerson> userCityList = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            List<ExcelCity> userCityChangeList = getUserCityChange(userCityList);
            userCityChangeListAll.addAll(userCityChangeList);
        }
        List<ExcelCity> ll = userCityChangeListAll.stream().filter(a -> a.getCityCodeMain().equals("350400")).collect(Collectors.toList());
        Map<Integer, List<ExcelCity>> map = ll.stream().collect(Collectors.groupingBy(ExcelCity::getYear));
        return userCityChangeListAll;
    }

    /**
     * 判断并标记一个发明家在所属城市的迁入迁出情况
     * 1、只有1条记录  标记为迁入
     * 2、变换城市时，该年新增一条迁出旧城市，一条迁入新城市
     *
     * @param userCityList
     */
    private static List<ExcelCity> getUserCityChange(List<ExcelPerson> userCityList) {
        List<ExcelCity> changeTypeList = new ArrayList<>();
        ExcelPerson current = null;
        for (int i = 0; i < userCityList.size(); i++) {
            ExcelPerson city = userCityList.get(i);
            if (i == 0) {
                ExcelCity qianru_1 = new ExcelCity();
                BeanUtils.copyProperties(city, qianru_1);
                qianru_1.setChangeType("迁入");
                qianru_1.setName(city.getName() + "_" + city.getUserCode());
                changeTypeList.add(qianru_1);
                current = city;
            } else if (!current.getCityCodeMain().equals(city.getCityCodeMain())) {
                // 认为发明家 在该年 迁出旧城市 新增迁出记录
                if (i != userCityList.size() - 1) {
                    ExcelCity qianchu = new ExcelCity();
                    BeanUtils.copyProperties(current, qianchu);
                    qianchu.setChangeType("迁出");
                    qianchu.setYear(city.getYear());
                    qianchu.setName(city.getName() + "_" + city.getUserCode());
                    changeTypeList.add(qianchu);
                }
                // 认为发明家迁入新城市  并记录迁入
                ExcelCity qianru_2 = new ExcelCity();
                BeanUtils.copyProperties(city, qianru_2);
                qianru_2.setChangeType("迁入");
                qianru_2.setName(city.getName() + "_" + city.getUserCode());
                changeTypeList.add(qianru_2);
                current = city;
            }
        }
        return changeTypeList;
    }

    public static void main(String[] args) {
        String excelFilePath = "F:\\excel\\210908\\inventor_symbol.xlsx";
        PersonListener listener = build(excelFilePath);
        List<ExcelCity> cityChangeList = buildCityTypeList(listener.getPersonList());

        String excelWritePath = "F:\\excel\\211015\\person_city_change_all.xlsx";
        ExcelTool.write(excelWritePath, cityChangeList, ExcelCity.class);

        String en_rd_person_excel_path = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        CountPersonNameDTO dto = PersonPatentHandle.getFilterMoveDTO(en_rd_person_excel_path);
        Map<String, List<ExcelPatent>> patentList = dto.getYesMove();

//        // 按照城市、年份分组
//        Map<Integer, Map<String, List<ExcelCity>>> cityYearMap = cityChangeList.parallelStream()
//                .collect(Collectors.groupingBy(ExcelCity::getYear, Collectors.groupingBy(city -> {
//                    return city.getCity() + "_" + city.getCityCodeMain();
//                })));
//        // 获取按年份统计 每个城市  迁入迁出的数据
//        List<CityCountDTO> dtoList = new ArrayList<>();
//        for (Map.Entry<Integer, Map<String, List<ExcelCity>>> yearEntry : cityYearMap.entrySet()) {
//            Integer year = yearEntry.getKey();
//            for (Map.Entry<String, List<ExcelCity>> cityEntry : yearEntry.getValue().entrySet()) {
//                String key = cityEntry.getKey();
//                String[] arr = key.split("_");
//                Map<String, List<ExcelCity>> typeMap = cityEntry.getValue().parallelStream().collect(Collectors.groupingBy(ExcelCity::getChangeType));
//                if (typeMap == null || typeMap.size() < 1) {
//                    continue;
//                }
//                if (typeMap.get("迁入") != null) {
//                    List<String> comeList = typeMap.get("迁入").parallelStream().map(ExcelPerson::getName).collect(Collectors.toList());
//                    CityCountDTO come_dto = new CityCountDTO();
////                    come_dto.setUserList(comeList);
//                    come_dto.setYear(year);
//                    come_dto.setCity(arr[0]);
//                    come_dto.setCityCode(arr[1]);
//                    come_dto.setType("迁入");
//                    dtoList.add(come_dto);
//                }
//                if (typeMap.get("迁出") != null) {
//                    List<String> goList = typeMap.get("迁出").parallelStream().map(ExcelPerson::getName).collect(Collectors.toList());
//                    CityCountDTO go_dto = new CityCountDTO();
////                    go_dto.setUserList(goList);
//                    go_dto.setYear(year);
//                    go_dto.setCity(arr[0]);
//                    go_dto.setCityCode(arr[1]);
//                    go_dto.setType("迁出");
//                    dtoList.add(go_dto);
//                }
//            }
//        }
//        dtoList = dtoList.parallelStream().sorted(Comparator.comparing(CityCountDTO::getCityCode).thenComparing(CityCountDTO::getYear)).collect(Collectors.toList());
//        String excelWritePath = "F:\\excel\\210908\\city_count.xlsx";
//        ExcelTool.write(excelWritePath, dtoList, CityCountDTO.class);


    }


}
