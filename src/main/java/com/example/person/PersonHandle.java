package com.example.person;

import com.example.person.entity.dto.UserSymbolYearDTO;
import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonHandle {

    public void setYesMoveUserCode(List<ExcelPatent> personList) {
        String userCode = createUserCode("Y");
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    public void setNoMoveUserCode(List<ExcelPatent> personList) {
        String userCode = createUserCode("N");
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    public void setUserCode(List<ExcelPatent> personList, String userCode) {
        personList.stream().forEach(item -> item.setUserCode(userCode));
    }

    private static int number = 10000000;

    private String createUserCode(String type) {
        number++;
        return type + number;
    }

    /**
     * 获取一个发明人在每个公司的开始时间和结束时间<br/>
     * key->symbol<br/>
     * value->人员城市及专利信息<br/>
     *
     * @param symbolMap
     * @return
     */
    public static List<UserSymbolYearDTO> buildCheckList(Map<String, List<ExcelPatent>> symbolMap) {
        List<UserSymbolYearDTO> l = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : symbolMap.entrySet()) {
            List<ExcelPatent> list = entry.getValue().stream().sorted(Comparator.comparing(ExcelPerson::getYear)).collect(Collectors.toList());
            UserSymbolYearDTO dto = new UserSymbolYearDTO();
            dto.setSymbol(entry.getKey());
            dto.setMinYear(list.get(0).getYear());
            dto.setMaxYear(list.get(list.size() - 1).getYear());
            l.add(dto);
        }
        return l;
    }

    /**
     * 通过一个发明人的全部公司的开始和结束年份，判断是否是同一人
     *
     * @param list
     * @return
     */
    public static boolean checkIsSame(List<UserSymbolYearDTO> list) {
        list = list.stream().sorted(Comparator.comparing(UserSymbolYearDTO::getMinYear)).collect(Collectors.toList());
        boolean isOne = true;
        for (int i = 0; i < list.size(); i++) {
            UserSymbolYearDTO currDto = list.get(i);
            // 排除一个公司的数据
            List<UserSymbolYearDTO> temp = list.stream().filter(entry -> !entry.getSymbol().equals(currDto.getSymbol())).collect(Collectors.toList());
            if (temp.size() > 0) {
                for (UserSymbolYearDTO otherSymbol : temp) {
                    // 最大年份 > 其他公司的最小年份/最大年份 > 最小年份  不是同一个人
                    if ((currDto.getMaxYear() >= otherSymbol.getMinYear() && otherSymbol.getMinYear() >= currDto.getMinYear())) {
                        isOne = false;
                    }
                    if (currDto.getMaxYear() >= otherSymbol.getMaxYear() && otherSymbol.getMaxYear() >= currDto.getMinYear()) {
                        isOne = false;
                    }
                    // 最大年份 = 其他公司最小年份  专利不同   不是同一人
                    // 最大年份 = 其他公司最小年份  @TODO(专利相同)   是同一人
                    // 最大年份 < 其他公司最小年份   是同一人
                }
                list.remove(currDto);
                i--;
            }
        }
        return isOne;
    }
}
