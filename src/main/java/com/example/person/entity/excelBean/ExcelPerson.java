package com.example.person.entity.excelBean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExcelPerson {
    @ExcelProperty("year")
    Integer year;
    @ExcelProperty("symbol")
    String symbol;
    @ExcelProperty("name")
    String name;
    /**
     * 专利总数
     **/
    @ExcelProperty("patent_sum")
    Integer patentSum;
    /**
     * 发明申请总数
     **/
    @ExcelProperty("invention_sum")
    Integer inventionSum;
    /**
     * 实用新型总数
     **/
    @ExcelProperty("utility_model_sum")
    Integer utilityModelSum;
    /**
     * 外观设计总数
     **/
    @ExcelProperty("design_sum")
    Integer designSum;
    /**
     * 引用次数
     **/
    @ExcelProperty("quote_sum")
    Integer quoteSum;
    /**
     * 城市编码
     **/
    @ExcelProperty("city_code_main")
    String cityCodeMain;
    /**
     * 城市编码
     **/
    @ExcelProperty("city")
    String city;

    public static void main(String[] args) {
        List<String> l = new ArrayList<>();
        l.add("2018_北京");
        l.add("2018_南京");
        l.add("2015_北京");
        l.add("2016_天津");
        l.add("2020_天津");
        l.add("2012_郑州");

        Collections.sort(l, (s1, s2) -> s1.compareTo(s2));
        l.stream().forEach(System.out::println);
    }
}
