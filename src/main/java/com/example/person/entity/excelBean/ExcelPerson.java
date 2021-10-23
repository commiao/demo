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
