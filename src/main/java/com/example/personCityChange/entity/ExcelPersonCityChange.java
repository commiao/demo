package com.example.personCityChange.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.personCity.entity.CityChangeDTO;
import lombok.Data;

@Data
public class ExcelPersonCityChange extends CityChangeDTO {
    @ExcelProperty("last_pm_core")
    private String lastPmCore;
    @ExcelProperty("now_pm_core")
    private String nowPmCore;
}
