package com.example.personCity;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.personCity.entity.CityChangeDTO;
import com.example.personCity.entity.CityCountDTO;
import com.example.personCity.entity.ExcelCity;
import com.example.personCity.listener.PersonListener;

import java.util.*;
import java.util.stream.Collectors;

public class CityChangeHandle {

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
    private static List<CityChangeDTO> findAllChangeByPerson(List<ExcelPerson> list) {
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

    /**
     * 一个城市、一个年的、迁入/迁出 统计
     *
     * @param cityList
     * @return
     */
    private static CityCountDTO buildCityCountDTO(List<ExcelCity> cityList) {
        Map<String, List<ExcelCity>> changeType_map = cityList.stream().collect(Collectors.groupingBy(ExcelCity::getChangeType));
        ExcelCity excelCity = cityList.get(0);
        CityCountDTO dto = new CityCountDTO();
        dto.setCity(excelCity.getCity());
        dto.setCityCode(excelCity.getCityCodeMain());
        dto.setYear(excelCity.getYear());
        int addTotal = changeType_map.get("迁入") == null ? 0 : changeType_map.get("迁入").size();
        dto.setAddTotal(addTotal);
        int decreaseTotal = changeType_map.get("迁出") == null ? 0 : changeType_map.get("迁出").size();
        dto.setDecreaseTotal(decreaseTotal);
        Integer total = dto.getAddTotal() - dto.getDecreaseTotal();
        dto.setType(total < 0 ? "迁出" : "迁入");
        dto.setTotal(total);
        return dto;
    }

    private static List<CityCountDTO> findAllChangeByCity(List<ExcelPerson> list, boolean isFilter) {
        Map<String, Map<Integer, List<ExcelCity>>> result = new HashMap<>();
        List<ExcelCity> personCityList = PersonCityHandle.buildCityTypeList(list);
        // 每个发明人的城市迁移数据
        Map<String, List<ExcelCity>> cityListForPerson = personCityList.stream().collect(Collectors.groupingBy(ExcelCity::getCityCodeMain));
        for (Map.Entry<String, List<ExcelCity>> entry : cityListForPerson.entrySet()) {
            // 已经完成对交叉年份的去重工作
            List<ExcelCity> personList = entry.getValue().stream().sorted(Comparator.comparing(ExcelCity::getYear)).collect(Collectors.toList());
            String currentCityCode = null;
            // 将每条数据添加到
            int count = 1;
            int total = personList.size();
            for (ExcelCity city : personList) {
                // 只有1条数据不排除
                if (total != 1 && (count == 1 || count == total) && isFilter) {
                    count++;
                    continue;
                }
                currentCityCode = city.getCityCodeMain();
                Map<Integer, List<ExcelCity>> cityMap = result.get(currentCityCode);
                if (cityMap == null) {
                    cityMap = new HashMap<>();
                }
                Integer currentYear = city.getYear();
                List<ExcelCity> city_year_person_list = cityMap.get(currentYear);
                if (city_year_person_list == null) {
                    city_year_person_list = new ArrayList<>();
                }
                city_year_person_list.add(city);
                cityMap.put(currentYear, city_year_person_list);
                result.put(currentCityCode, cityMap);
                count++;
            }
        }
        List<CityCountDTO> resultList = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, List<ExcelCity>>> city_map : result.entrySet()) {
            for (Map.Entry<Integer, List<ExcelCity>> year_map : city_map.getValue().entrySet()) {
                resultList.add(buildCityCountDTO(year_map.getValue()));
            }
        }
        return resultList.stream().sorted(Comparator.comparing(CityCountDTO::getCity).thenComparing(CityCountDTO::getYear)).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String excelFilePath = "F:\\excel\\211024\\yes_move.xlsx";
        PersonListener listener = build(excelFilePath);


        // 按人员维度统计
//        List<CityChangeDTO> cityChangeList = findAllChangeByPerson(listener.getPersonList());
//        String excelWritePath = "F:\\excel\\211015\\person_city_change.xlsx";
//        ExcelTool.write(excelWritePath, cityChangeList, CityChangeDTO.class);

        // 按城市维度统计
        String excelWritePath_city = "F:\\excel\\211024\\person_move_count_for_city.xlsx";
        List<CityCountDTO> cityCountDTOList = findAllChangeByCity(listener.getPersonList(), false);
        ExcelTool.write(excelWritePath_city, cityCountDTOList, CityCountDTO.class);
        // 按城市维度统计 过滤首年和末年
        String excelWritePath_city_filter = "F:\\excel\\211024\\person_move_count_for_city_filter.xlsx";
        List<CityCountDTO> cityCountDTOList_isfilter = findAllChangeByCity(listener.getPersonList(), true);
        ExcelTool.write(excelWritePath_city_filter, cityCountDTOList_isfilter, CityCountDTO.class);
    }


}
