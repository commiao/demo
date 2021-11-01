package com.example.ins;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.ExcelTool;
import com.example.patent.entity.importDTO.ImportCompanyData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsAppPlanBookHandle {

    public static InsAppPlanBookListener buildInsAppPlanBookListener(String path) {
        ExcelReader excelReader = ExcelTool.getExcelReader(path);
        InsAppPlanBookListener listener = new InsAppPlanBookListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(InsAppPlanBook.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    public static void writeTxt(String content, String filePath) throws IOException {

        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();

        // write
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.flush();
        bw.close();
        fw.close();

    }

    private static String buildUpdateSql(InsAppPlanBook book) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(book.getUpdateTime());
        StringBuffer sb = new StringBuffer("update ins_app_plan_book set update_time = '")
                .append(str)
                .append("' where id = ")
                .append(book.getId())
                .append(";\r\n");
        return sb.toString();
    }


    public static void main(String[] args) {
        String nowPath = "F:\\excel\\ins-app-plan-book\\now.xlsx";
        InsAppPlanBookListener nowListen = buildInsAppPlanBookListener(nowPath);
        List<InsAppPlanBook> nowList = nowListen.getBookList();
        nowList.sort(Comparator.comparing(InsAppPlanBook::getId));

        String oldPath = "F:\\excel\\ins-app-plan-book\\old.xlsx";
        InsAppPlanBookListener oldListen = buildInsAppPlanBookListener(oldPath);
        List<InsAppPlanBook> oldList = oldListen.getBookList();
        Map<Integer, Date> oldMap = oldList.stream().collect(Collectors.toMap(InsAppPlanBook::getId, InsAppPlanBook::getUpdateTime));

        System.out.println("开始合并数据==============nowList：" + nowList.size() + "_________oldList：" + oldList.size());
        StringBuffer sb = new StringBuffer();
        for (InsAppPlanBook book : nowList) {
            Date oldDate = oldMap.get(book.getId());
            book.setUpdateTime(oldDate);
            sb.append(buildUpdateSql(book));
        }
        System.out.println("开始写入sql==============");
        String path = "F:\\excel\\ins-app-plan-book\\test3.txt";
        String content = sb.toString();
        try {
            writeTxt(content, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("结束==============");

    }

}
