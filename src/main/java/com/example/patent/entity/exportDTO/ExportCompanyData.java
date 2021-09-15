package com.example.patent.entity.exportDTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExportCompanyData {
    @ExcelProperty({"年份"})
    Integer year;
    @ExcelProperty({"上市工商代码"})
    String id;
    @ExcelProperty({"专利总数"})
    Integer patentCount;
    @ExcelProperty({"发明申请"})
    Integer inventionCount;
    @ExcelProperty({"实用新型"})
    Integer utilityModelCount;
    @ExcelProperty({"外观设计"})
    Integer designCount;
    @ExcelProperty({"引用次数"})
    Integer useCount;

    @ExcelProperty({"公司全称"})
    String name;
    @ExcelProperty({"公司简称"})
    String shortName;
    @ExcelProperty({"所属行业"})
    String industryName;
    @ExcelProperty({"省份"})
    String province;
    @ExcelProperty({"城市"})
    String city;
    @ExcelProperty({"企业性质"})
    String equityNature;
    @ExcelProperty({"申请状态"})
    String aat = "已申请";
}
