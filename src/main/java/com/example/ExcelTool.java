package com.example;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.commons.collections4.CollectionUtils;
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

    public static <T> void write(String excelWritePath, List<T> list, Class<T> clazz) {
        List<ExcelWriteBean> beanList = new ArrayList<ExcelWriteBean>() {{
            add(buildExcelWriteBean(null, null, list, clazz));
        }};
        write(excelWritePath, beanList);
    }

    public static <T> void write(String excelWritePath, Integer sheetNo, String sheetName, List<T> list, Class<T> clazz) {
        List<ExcelWriteBean> beanList = new ArrayList<ExcelWriteBean>() {{
            add(buildExcelWriteBean(sheetNo, sheetName, list, clazz));
        }};
        write(excelWritePath, beanList);
    }

    public static <T> ExcelWriteBean<T> buildExcelWriteBean(Integer sheetNo, String sheetName, List<T> list, Class<T> clazz) {
        ExcelWriteBean<T> bean = new ExcelWriteBean<>();
        bean.setSheetNo(sheetNo);
        bean.setSheetName(sheetName);
        bean.setList(list);
        bean.setClazz(clazz);
        return bean;
    }

    public static void write(String excelWritePath, List<ExcelWriteBean> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            ExcelWriter excelWriter = EasyExcel.write(excelWritePath).build();
            WriteSheet writeSheet = null;
            for (int i = 0; i < list.size(); i++) {
                ExcelWriteBean bean = list.get(i);
                Integer sheetNo = bean.getSheetNo() == null ? i : bean.getSheetNo();
                String sheetName = StringUtils.isBlank(bean.getSheetName()) ? "sheet" + i : bean.getSheetName();
                List dataList = bean.getList();
                Class clazz = bean.getClazz();
                writeSheet = EasyExcel.writerSheet(sheetNo, sheetName).head(clazz).build();
                excelWriter.write(dataList, writeSheet);
            }
            excelWriter.finish();
        }
    }

}
