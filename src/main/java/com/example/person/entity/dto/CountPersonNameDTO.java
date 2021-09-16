package com.example.person.entity.dto;

import com.example.person.entity.excelBean.ExcelPatent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class CountPersonNameDTO {
    /**
     * 未迁移
     */
    Map<String, List<ExcelPatent>> noMove;
    /**
     * 已迁移
     */
    Map<String, List<ExcelPatent>> yesMove;
    /**
     * 有问题
     */
    Map<String, List<ExcelPatent>> todoData;
}
