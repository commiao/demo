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
import com.example.personCityChange.PersonCityChangeHandle;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理发明人与城市之间的逻辑
 */
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
//        String excelFilePath = "F:\\excel\\210908\\inventor_symbol.xlsx";
//        PersonListener listener = build(excelFilePath);
//        List<ExcelCity> cityChangeList = buildCityTypeList(listener.getPersonList());
//
//        String excelWritePath = "F:\\excel\\211015\\person_city_change_all.xlsx";
//        ExcelTool.write(excelWritePath, cityChangeList, ExcelCity.class);

//        String en_rd_person_excel_path = "F:\\commiao_public\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        String en_rd_person_excel_path = "F:\\share with me\\public\\小井\\jing_处理好的数据\\210908\\en_rd_person.xlsx";
        List<ExcelPatent> allPersonPatentList = PersonPatentHandle.getAllPersonPatent(en_rd_person_excel_path);
        // 去除无效数据
        CountPersonNameDTO dto = PersonCityChangeHandle.buildPersonList(allPersonPatentList);
        List<ExcelPerson> yes_move_list = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : dto.getYesMoveMap().entrySet()) {
            List<ExcelPerson> list = entry.getValue().stream().map(patent -> {
                ExcelPerson person = new ExcelPerson();
                BeanUtils.copyProperties(patent, person);
                return person;
            }).collect(Collectors.toList());
            yes_move_list.addAll(list);
        }

        List<ExcelCity> cityList = getUserCityChange(yes_move_list);
        String person_city_change_yes_Path = "F:\\excel\\211024\\person_city_change_yes.xlsx";
        ExcelTool.write(person_city_change_yes_Path, cityList, ExcelCity.class);


    }

}
