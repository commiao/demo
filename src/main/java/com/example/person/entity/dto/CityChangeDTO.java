package com.example.person.entity.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.person.entity.excelBean.ExcelPatent;
import lombok.Data;

@Data
public class CityChangeDTO extends ExcelPatent {
    /**
     * 迁入/迁出
     **/
    @ExcelProperty("chang_type")
    String changType;
}
