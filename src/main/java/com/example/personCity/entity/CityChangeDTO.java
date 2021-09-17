package com.example.personCity.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CityChangeDTO {
    @ExcelProperty("year")
    private Integer year;
    @ExcelProperty("userCode")
    private String userCode;
    @ExcelProperty("name")
    private String name;
    @ExcelProperty("lastCityCode")
    private String lastCityCode;
    @ExcelProperty("lastCity")
    private String lastCity;
    @ExcelProperty("lastSymbol")
    private String lastSymbol;
    @ExcelProperty("nowCity")
    private String nowCity;
    @ExcelProperty("nowCityCode")
    private String nowCityCode;
    @ExcelProperty("nowSymbol")
    private String nowSymbol;
}
