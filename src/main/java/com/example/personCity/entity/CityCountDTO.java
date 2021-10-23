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
    /**
     * 整体是流入/流出
     */
    @ExcelProperty("type")
    String type;
    /**
     * 净流入流出数量
     */
    @ExcelProperty
    Integer total;
    @ExcelProperty("add_total")
    Integer addTotal;
    @ExcelProperty("decrease_total")
    Integer decreaseTotal;
}
