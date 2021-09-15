package com.example;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ExcelTool {
    public static ExcelReader getExcelReader(String excelPath) {
        File file = new File(excelPath);

        ExcelReader excelReader = null;
        try {
            excelReader = EasyExcel.read(new FileInputStream(file)).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return excelReader;
    }

    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
                //文件名，不包含路径
                //String fileName = tempList[i].getName();
            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了，
            }
        }
        return files;
    }

    public static <T> void write(String excelWritePath, List<T> list, Class<T> clazz) {
        write(excelWritePath, null, null, list, clazz);
    }

    private static <T> void write(String excelWritePath, Integer sheetNo, String sheetName, List<T> list, Class<T> clazz) {
        sheetNo = sheetNo == null ? 0 : sheetNo;
        sheetName = StringUtils.isBlank(sheetName) ? "sheet" : sheetName;
        ExcelWriter excelWriter = EasyExcel.write(excelWritePath).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo, sheetName).head(clazz).build();
        excelWriter.write(list, writeSheet);
        excelWriter.finish();
    }

}
