package com.example.person.entity.excelBean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelPerson {
    @ExcelProperty("year")
    Integer year;
    @ExcelProperty("symbol")
    String symbol;
    @ExcelProperty("name")
    String name;
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
    /**
     * 城市编码
     **/
    @ExcelProperty("city_code_main")
    String cityCodeMain;
    /**
     * 城市编码
     **/
    @ExcelProperty("city")
    String city;
}
