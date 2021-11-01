package com.example.ins;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class InsAppPlanBook {
    @ExcelProperty("id")
    Integer id;
    @ExcelProperty("update_time")
    Date updateTime;
}
