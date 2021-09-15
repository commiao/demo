package com.example.person.entity.excelBean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelPatent extends ExcelPerson {
    /**
     * 专利总数
     **/
    @ExcelProperty("patent_sum")
    Integer patentSum;
    /**
     * 发明申请总数
     **/
    @ExcelProperty("invention_sum")
    Integer inventionSum;
    /**
     * 实用新型总数
     **/
    @ExcelProperty("utility_model_sum")
    Integer utilityModelSum;
    /**
     * 外观设计总数
     **/
    @ExcelProperty("design_sum")
    Integer designSum;
    /**
     * 引用次数
     **/
    @ExcelProperty("quote_sum")
    Integer quoteSum;
}
