package com.example.personCity.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.person.entity.excelBean.ExcelPerson;
import lombok.Data;

@Data
public class ExcelCity extends ExcelPerson {
    /**
     * 迁移类型（迁入/迁出）
     */
    @ExcelProperty("change_type")
    String changeType;
}
