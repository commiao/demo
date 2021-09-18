package com.example.personCityChange.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class CityMoveCountDTO {
    @ExcelProperty("year")
    private Integer year;
    @ExcelProperty("good_to_bad_count")
    private int goodToBadCount;
    @ExcelProperty("bad_to_good_count")
    private int badToGoodCount;
}
