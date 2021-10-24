package com.example.person.entity.dto;

import com.example.person.entity.excelBean.ExcelPatent;
import com.example.person.entity.excelBean.ExcelPerson;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
public class CountPersonNameDTO {
    /**
     * 未迁移
     */
    Map<String, List<ExcelPatent>> noMoveMap;
    /**
     * 已迁移
     */
    Map<String, List<ExcelPatent>> yesMoveMap;
    /**
     * 有问题
     */
    Map<String, List<ExcelPatent>> todoDataMap;

    /**
     * 有迁移过的发明人数据
     *
     * @return
     */
    public List<ExcelPatent> getNoMoveList() {
        List<ExcelPatent> no_List = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : noMoveMap.entrySet()) {
            no_List.addAll(entry.getValue());
        }
        int i = no_List.size();
        System.out.println("######################未迁移数据" + i + "条");
        no_List.parallelStream().sorted(Comparator.comparing(ExcelPatent::getName).thenComparing(ExcelPerson::getYear)).collect(Collectors.toList());
        return no_List;
    }

    /**
     * 没有迁移过的发明人数据
     *
     * @return
     */
    public List<ExcelPatent> getYesMoveList() {
        List<ExcelPatent> yes_List = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : yesMoveMap.entrySet()) {
            yes_List.addAll(entry.getValue());
        }
        int j = yes_List.size();
        System.out.println("######################已迁移数据" + j + "条");
        yes_List.parallelStream().sorted(Comparator.comparing(ExcelPatent::getName).thenComparing(ExcelPerson::getYear)).collect(Collectors.toList());
        return yes_List;
    }

    /**
     * 待处理的发明人的数据
     *
     * @return
     */
    public List<ExcelPatent> getTodoList() {
        List<ExcelPatent> todo_List = new ArrayList<>();
        for (Map.Entry<String, List<ExcelPatent>> entry : todoDataMap.entrySet()) {
            todo_List.addAll(entry.getValue());
        }
        int j = todo_List.size();
        System.out.println("######################待处理数据" + j + "条");
        return todo_List;
    }
}
