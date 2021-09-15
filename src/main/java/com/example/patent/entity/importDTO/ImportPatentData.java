package com.example.patent.entity.importDTO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ImportPatentData {
    @ExcelProperty("工商上市代码")
    String ids;
    @ExcelProperty("申请号")
    String applyId;
    @ExcelProperty("专利类型")
    String type;
    @ExcelProperty("发明人")
    String names;
    @ExcelProperty("发明人数量")
    Integer count;
    @ExcelProperty("被引证次数")
    Integer useCount;
    @ExcelProperty("申请日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyDate;
    @ExcelProperty("公开（公告）日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date successDate;
}
