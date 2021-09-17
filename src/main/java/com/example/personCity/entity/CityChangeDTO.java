package com.example.personCity.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityChangeDTO {
    @ExcelProperty("year")
    private Integer year;
    @ExcelProperty("user_code")
    private String userCode;
    @ExcelProperty("name")
    private String name;
    @ExcelProperty("last_city_code")
    private String lastCityCode;
    @ExcelProperty("lastCity")
    private String lastCity;
    @ExcelProperty("last_symbol")
    private String lastSymbol;
    @ExcelProperty("now_city")
    private String nowCity;
    @ExcelProperty("now_city_code")
    private String nowCityCode;
    @ExcelProperty("now_symbol")
    private String nowSymbol;
}
