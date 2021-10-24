package com.example.personCityChange;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.ExcelWriteBean;
import com.example.person.PersonHandle;
import com.example.person.entity.dto.CountPersonNameDTO;
import com.example.person.entity.dto.UserSymbolYearDTO;
import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;
import com.example.personCityChange.entity.CityMoveCountDTO;
import com.example.personCityChange.entity.ExcelPersonCityChange;
import com.example.personCityChange.listener.PersonCityChangeListener;

import java.math.BigDecimal;
import java.util.*;
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

    // 1、发明人 年份  城市
    // 2、发明人-》所在城市超过2个的
    // 3、发明人去重-》同一年份，不同城市，发明类型不同
    // 4、发明人离开城市年份、进入城市年份
    // 5、汇总进入/离开同一城市的发明人集合，区分年份
    // 6、根据城市 筛选出 从高质量到低质量/从低质量到高质量的数据集合
    public static CountPersonNameDTO buildPersonList(List<ExcelPatent> list) {
        Map<String, List<ExcelPatent>> personMap = list.parallelStream().collect(Collectors.groupingBy(ExcelPatent::getName));
//        Iterator<Map.Entry<String, List<ExcelPatent>>> it = personMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, List<ExcelPatent>> entry = it.next();
//            if (!checkMoreCity(entry.getValue()) || checkSameName(entry.getValue())) {
//                it.remove();//使用迭代器的remove()方法删除元素
//            }
//        }
        System.out.println("========================共" + list.size() + "条数据，" + personMap.size() + "个人名");
        Map<String, List<ExcelPatent>> noMove = new HashMap<>();
        Map<String, List<ExcelPatent>> yesMove = new HashMap<>();
        Map<String, List<ExcelPatent>> todoData = new HashMap<>();
        PersonHandle personHandle = new PersonHandle();
        for (Map.Entry<String, List<ExcelPatent>> entry : personMap.entrySet()) {
            List<ExcelPatent> userList = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            Map<String, List<ExcelPatent>> symbolMap = userList.stream().collect(Collectors.groupingBy(ExcelPerson::getSymbol));
            if (symbolMap.size() == 1) {
                // 一个人名只在一个城市出现过
                personHandle.setNoMoveUserCode(userList);
                noMove.put(userList.get(0).getName(), userList);
            } else {
                // 一个人名出现在多个城市
                List<UserSymbolYearDTO> checkList = PersonHandle.buildCheckList(symbolMap);
                if (PersonHandle.checkIsSame(checkList)) {
                    // 1、时间没有交集  迁移了
                    personHandle.setYesMoveUserCode(userList);
                    yesMove.put(userList.get(0).getName(), userList);
                } else {
                    // 2、时间有交集 @TODO 按重名剔除
                    personHandle.setUserCode(userList, "******");
                    todoData.put(userList.get(0).getName(), userList);
                }
            }
        }
        System.out.println("========================未迁移人数" + noMove.size() + "个人，已迁移人数" + yesMove.size() + "个人，有问题人数" + todoData.size() + "个人");
        return CountPersonNameDTO.builder().noMoveMap(noMove).yesMoveMap(yesMove).todoDataMap(todoData).build();
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
