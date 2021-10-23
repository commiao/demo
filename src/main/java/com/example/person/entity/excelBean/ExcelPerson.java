package com.example.person.entity.excelBean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ExcelPerson {
    @ExcelProperty("year")
    Integer year;
    @ExcelProperty("symbol")
    String symbol;
    @ExcelProperty("name")
    String name;
    /**
     * 用户唯一标识
     */
    @ExcelProperty("user_code")
    String userCode;
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
    /**
     * 专利总数
     */
    @ExcelProperty("patent_sum")
    String patentSum;
    /**
     * 发明总数
     */
    @ExcelProperty("invention_sum")
    String inventionSum;
    /**
     * 实用新型
     */
    @ExcelProperty("utility_model_sum")
    String utilityModelSum;
    /**
     * 设计模式
     */
    @ExcelProperty("design_sum")
    String designSum;
    /**
     * 引用次数
     */
    @ExcelProperty("quote_sum")
    String quoteSum;


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
