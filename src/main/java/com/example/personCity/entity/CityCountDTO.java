package com.example.personCity.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CityCountDTO {
    @ExcelProperty("year")
    Integer year;
    @ExcelProperty("city")
    String city;
    @ExcelProperty("city_code")
    String cityCode;
    @ExcelProperty("type")
    String type;
    @ExcelProperty
    Integer total;
}
