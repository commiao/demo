package com.example;

import lombok.Data;

import java.util.List;

@Data
public class ExcelWriteBean<T> {
    Integer sheetNo;
    String sheetName;
    List<T> list;
    Class<T> clazz;
}
