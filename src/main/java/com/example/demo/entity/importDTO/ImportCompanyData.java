package com.example.demo.entity.importDTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ImportCompanyData {
    /**
     * 工商上市代码
     **/
    @ExcelProperty("id")
    String id;
    /**
     * 公司完整名称
     **/
    @ExcelProperty("name")
    String name;
    /**
     * 公司简称
     **/
    @ExcelProperty("ShortName")
    String shortName;
    /**
     * 所属行业
     **/
    @ExcelProperty("IndustryName")
    String industryName;
    /**
     * 省份
     **/
    @ExcelProperty("PROVINCE")
    String province;
    /**
     * 城市
     **/
    @ExcelProperty("CITY")
    String city;
    /**
     * 公司性质
     **/
    @ExcelProperty("EquityNature")
    private String equityNature;
}
