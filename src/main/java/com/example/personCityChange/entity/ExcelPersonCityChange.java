package com.example.personCityChange.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.personCity.entity.CityChangeDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExcelPersonCityChange extends CityChangeDTO {
    @ExcelProperty("last_pm_score")
    private BigDecimal lastPmScore;
    @ExcelProperty("now_pm_score")
    private BigDecimal nowPmScore;
}
