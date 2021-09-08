package com.example;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.demo.entity.bean.CountDTO;
import com.example.demo.entity.bean.PatentRow;
import com.example.demo.entity.bean.PersonRow;
import com.example.demo.entity.exportDTO.ExportCompanyData;
import com.example.demo.entity.exportDTO.ExportPersonData;
import com.example.demo.entity.importDTO.ImportCompanyData;
import com.example.demo.entity.importDTO.ImportPatentData;
import com.example.demo.listener.CompanyListener;
import com.example.demo.listener.PersonListener;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static ExportPersonData buildExportData(Map<String, List<PersonRow>> typeMap, Integer year, ImportCompanyData companyInfo) {
        if (year == null || companyInfo == null
                || StringUtils.isBlank(companyInfo.getId()) || StringUtils.isBlank(companyInfo.getName())) {
            System.out.println("###################");
        }
        ExportPersonData data = ExportPersonData.builder()
                .year(year)
                .id(companyInfo.getId()).name(companyInfo.getName())
                .industryName(companyInfo.getIndustryName()).equityNature(companyInfo.getEquityNature())
                .province(companyInfo.getProvince()).city(companyInfo.getCity())
                .aat("已申请")
                .build();
        CountDTO dto = buildCountDTOForPerson(typeMap);
        data.setInventor(dto.getInventor());
        data.setInventionCount(dto.getInventionCount());
        data.setUtilityModelCount(dto.getUtilityModelCount());
        data.setDesignCount(dto.getDesignCount());
        data.setPatentCount(dto.getPatentCount());
        data.setUseCount(dto.getUseCount());
        return data;
    }

    private static CountDTO buildCountDTOForPerson(Map<String, List<PersonRow>> typeMap) {
        CountDTO data = CountDTO.builder().build();
        Integer useCountTotal = 0;
        Integer totalCount = 0;
        String inventor = "";
        for (Map.Entry<String, List<PersonRow>> entry : typeMap.entrySet()) {
            String type = entry.getKey();
            List<PersonRow> personRowList = entry.getValue();
            Integer useCount = personRowList.stream().filter(personRow -> personRow.getUseCount() != null).mapToInt(PersonRow::getUseCount).sum();
            Integer row = personRowList == null || personRowList.size() < 1 ? 0 : personRowList.size();
            if (!inventor.equals(personRowList.get(0).getName())) {
                inventor = inventor + personRowList.get(0).getName();
                data.setInventor(inventor);
            }
            if ("发明申请".equals(type)) {
                data.setInventionCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
            if ("实用新型".equals(type)) {
                data.setUtilityModelCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
            if ("外观设计".equals(type)) {
                data.setDesignCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
        }
        data.setPatentCount(totalCount);
        data.setUseCount(useCountTotal);
        return data;
    }

    private static CountDTO buildCountDTOForPatent(Map<String, List<PatentRow>> typeMap) {
        CountDTO data = CountDTO.builder().build();
        Integer useCountTotal = 0;
        Integer totalCount = 0;
        String inventor = "";
        for (Map.Entry<String, List<PatentRow>> entry : typeMap.entrySet()) {
            String type = entry.getKey();
            List<PatentRow> personRowList = entry.getValue();
            Integer useCount = personRowList.stream().filter(personRow -> personRow.getUseCount() != null).mapToInt(PatentRow::getUseCount).sum();
            Integer row = personRowList == null || personRowList.size() < 1 ? 0 : personRowList.size();
            if (!inventor.equals(personRowList.get(0).getNames())) {
                inventor = inventor + personRowList.get(0).getNames();
                data.setInventor(inventor);
            }
            if ("发明申请".equals(type)) {
                data.setInventionCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
            if ("实用新型".equals(type)) {
                data.setUtilityModelCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
            if ("外观设计".equals(type)) {
                data.setDesignCount(row);
                totalCount += row;
                useCountTotal += useCount;
            }
        }
        data.setPatentCount(totalCount);
        data.setUseCount(useCountTotal);
        return data;
    }

    private static CountDTO buildCountDTO(List<PatentRow> personList) {
        // 按专利类型分组
        Map<String, List<PatentRow>> typeMap = personList.parallelStream().collect(Collectors.groupingBy(PatentRow::getType));
        return buildCountDTOForPatent(typeMap);
    }

    /**
     * 获取单个excel中的每一行发明专利
     *
     * @param excelPath
     * @return
     */
    private static PersonListener getPersonListener(String excelPath) {
        System.out.println("=======================================" + excelPath);
        ExcelReader excelReader = getExcelReader(excelPath);
        PersonListener personListener = new PersonListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ImportPatentData.class).registerReadListener(personListener).build();
        excelReader.read(readSheetSys);
        String fileName = excelPath.trim();
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf("_"));
        personListener.buildCompanyNo(fileName);
        return personListener;
    }

    public static List<ExportPersonData> buildExportPersonDataList(List<PersonRow> personList, Map<String, ImportCompanyData> companyInfoMap) {
        // 工商代码-》发明人-》年份-》专利类型
        Map<String, Map<String, Map<Integer, Map<String, List<PersonRow>>>>> totalMap = personList.parallelStream()
                .collect(Collectors.groupingBy(PersonRow::getCompanyNo, Collectors.groupingBy(PersonRow::getName, Collectors.groupingBy(PersonRow::getYear, Collectors.groupingBy(PersonRow::getType)))));
        List<ExportPersonData> list = new ArrayList<>();
        totalMap.forEach((companyNo, nameMap) -> {
            nameMap.forEach((name, yearMap) -> {
                yearMap.forEach((year, typeMap) -> {
                    ImportCompanyData companyInfo = companyInfoMap.get(companyNo);
                    if (companyInfo == null || StringUtils.isBlank(companyInfo.getId())) {
                        System.out.println("=========================汇总表中不存在：" + companyNo);
                    }
                    list.add(buildExportData(typeMap, year, companyInfo));
                });
            });
        });
        list.sort(Comparator.comparing(ExportPersonData::getId)
                .thenComparing(ExportPersonData::getName)
                .thenComparing(ExportPersonData::getYear));
        return list;
    }

    private static CompanyListener buildCompanyListener(String excelPath) {
        ExcelReader excelReader = ExcelTool.getExcelReader(excelPath);
        CompanyListener listener = new CompanyListener();
        ReadSheet readSheetSys = EasyExcel.readSheet().head(ImportCompanyData.class).registerReadListener(listener).build();
        excelReader.read(readSheetSys);
        return listener;
    }

    public static List<ExportCompanyData> buildExportCompanyDataList(List<ImportCompanyData> companyList, Map<String, List<PatentRow>> companyMap) {
        List<ExportCompanyData> exportCompanyDataList = new ArrayList<>();
        String companyNo = null;
        for (ImportCompanyData company : companyList) {
            companyNo = company.getId();
            if (companyMap.get(companyNo) != null) {
                List<PatentRow> list = companyMap.get(companyNo);
                // 按年份  将专利数据分组
                Map<Integer, List<PatentRow>> yearMap = list.parallelStream().collect(Collectors.groupingBy(PatentRow::getYear));
                yearMap.forEach((year, personList) -> {
                    ExportCompanyData data = ExportCompanyData.builder()
                            .id(company.getId()).name(company.getName()).shortName(company.getShortName())
                            .industryName(company.getIndustryName()).equityNature(company.getEquityNature())
                            .city(company.getCity()).province(company.getProvince()).aat("已申请")
                            .build();
                    // 统计一年的专利数据
                    CountDTO dto = buildCountDTO(personList);
                    data.setYear(year);
                    data.setInventionCount(dto.getInventionCount());
                    data.setUtilityModelCount(dto.getUtilityModelCount());
                    data.setDesignCount(dto.getDesignCount());
                    data.setPatentCount(dto.getPatentCount());
                    data.setUseCount(dto.getUseCount());
                    exportCompanyDataList.add(data);
                });

            }
        }
        return exportCompanyDataList;
    }

    private static void buildFile(List<String> personFiles, List<ImportCompanyData> companyList) {
        Map<String, ImportCompanyData> companyNameMap = companyList.parallelStream().collect(Collectors.toMap(ImportCompanyData::getName, Function.identity(), (key1, key2) -> key2));
        String filePath = null;
        String fileName = null;
        String companyId = null;
        String key = null;
        for (String url : personFiles) {
            File personFile = new File(url);
            filePath = personFile.getParent();
            fileName = personFile.getName();
            key = fileName.substring(0, fileName.lastIndexOf("."));
            ImportCompanyData company = companyNameMap.get(key);
            if (company == null || StringUtils.isBlank(company.getId())) {
                System.out.println("======================" + key);
                continue;
            }
            companyId = company.getId();
            File newFile = new File(filePath + "\\temp\\" + companyId + "_" + fileName.trim());
            try {
                Files.copy(personFile.toPath(), newFile.toPath());
            } catch (IOException e) {
                System.out.println(personFile.getName());
                e.printStackTrace();
            }
        }
    }

    public static void write(List<PersonRow> personList, List<PatentRow> patentList, List<ImportCompanyData> companyList) {
        // excel输出地址
        String excelWritePath = "F:\\excel\\write\\test.xls";
        ExcelWriter excelWriter = EasyExcel.write(excelWritePath).build();

        // 获取全部公司集合 上市代码-》公司信息
        Map<String, ImportCompanyData> companyIdMap = companyList.parallelStream().collect(Collectors.toMap(ImportCompanyData::getId, Function.identity(), (key1, key2) -> key2));
        // 获取个人处理后的列表
        List<ExportPersonData> personDataList = buildExportPersonDataList(personList, companyIdMap);


        personDataList = personDataList.subList(0, 60000);
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "inventor").head(ExportPersonData.class).build();
        excelWriter.write(personDataList, writeSheet);

        // 将专利按公司分组  上市代码-》专利集合
        Map<String, List<PatentRow>> companyPersonMap = patentList.parallelStream().collect(Collectors.groupingBy(PatentRow::getCompanyNo));
        List<ExportCompanyData> companyDataList = buildExportCompanyDataList(companyList, companyPersonMap);
        writeSheet = EasyExcel.writerSheet(1, "enterprise").head(ExportCompanyData.class).build();
        excelWriter.write(companyDataList, writeSheet);

        excelWriter.finish();
    }

    public static void main(String[] args) {

//        String excelPath_person = "F:\\excel\\person";
//        List<String> personFileList = getFiles(excelPath_person);

        String excelPath_company = "F:\\excel\\企业专利数据.xlsx";
        CompanyListener companyListener = buildCompanyListener(excelPath_company);
        List<ImportCompanyData> companyList = companyListener.getCompanyList();
        // 初始化文件，生产带上市代码的文件集合
//        buildFile(personFileList, companyList);

        // 获取全部文件集合
        String excelPath_person_build = "F:\\excel\\person\\temp";
        List<String> personFileList_build = getFiles(excelPath_person_build);

        List<PersonRow> personList = new ArrayList<>();
        List<PatentRow> patentList = new ArrayList<>();
        personFileList_build.stream().forEach(filePath -> {
            PersonListener personListener = getPersonListener(filePath);
            List<PersonRow> personRowList = personListener.getPersonRowList();
            personList.addAll(personRowList);
            List<PatentRow> patentRowList = personListener.getPatentList();
            patentList.addAll(patentRowList);
        });

        write(personList, patentList, companyList);
    }
}
