package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.bean.PatentRow;
import com.example.demo.entity.bean.PersonRow;
import com.example.Utils;
import com.example.demo.entity.importDTO.ImportPatentData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * excel监听：个人数据
 *
 * @author 井淼
 */
@Slf4j
public class PersonListener extends AnalysisEventListener<ImportPatentData> {
    // 发明专利数据集合
    private List<PatentRow> patentList = new ArrayList<>();
    // 发明人数据集合
    private List<PersonRow> personList = new ArrayList<>();

    @Override
    public void invoke(ImportPatentData importData, AnalysisContext analysisContext) {
//        log.debug("解析到一条数据:{}", JSONObject.toJSONString(importData));
        if (!Utils.checkObjAllFieldsIsNull(importData)) {
            if (StringUtils.isBlank(importData.getType()) || StringUtils.isBlank(importData.getIds()) || StringUtils.isBlank(importData.getNames())) {
                log.info("解析到一条异常数据:{}", JSONObject.toJSONString(importData));
            } else if (!"发明授权".equals(importData.getType()) && checkYear(importData.getApplyDate())) {
                Integer year = getYear(importData.getApplyDate());
                patentList.add(PatentRow.builder()
                        .year(year).names(importData.getNames())
                        .applyNo(importData.getApplyId()).companyNo(importData.getIds())
                        .type(importData.getType()).useCount(importData.getUseCount())
                        .build());
            }
        }
    }

    private boolean checkYear(Date applyDate) {
        Integer year = getYear(applyDate);
        return year > 2009 && year < 2019;

    }

    private static Integer getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        List<PatentRow> excelList = getPatentList();
        String companyNo = null;
        for (PatentRow patentRow : excelList) {
            if (companyNo == null) {
                String[] ids = patentRow.getCompanyNo().trim().split(";");
                for (String id : ids) {
                    if (id != null && !"".equals(id.trim())) {
                        companyNo = id;
                        break;
                    } else {
                        log.error("出现异常数据：{},{}", ids, JSONObject.toJSONString(patentRow));
                    }
                }
            }
            String[] names = patentRow.getNames().trim().split(";");
            for (String name : names) {
                if (name == null || "".equals(name.trim())) {
                    log.error("出现异常数据：{}.{}", names, JSONObject.toJSONString(patentRow));
                    continue;
                }
                personList.add(PersonRow.builder()
                        .year(patentRow.getYear()).name(name.trim())
                        .applyNo(patentRow.getApplyNo()).companyNo(companyNo)
                        .type(patentRow.getType()).useCount(patentRow.getUseCount())
                        .build());
            }
        }
        log.info("所有数据解析完成！");
    }

    public List<PatentRow> getPatentList() {
        return patentList;
    }

    public List<PersonRow> getPersonRowList() {
        return personList;
    }

    public void buildCompanyNo(String companyNo) {
        personList.parallelStream().forEach(personRow -> personRow.setCompanyNo(companyNo));
        patentList.parallelStream().forEach(patentRow -> patentRow.setCompanyNo(companyNo));
    }

    public static void main(String[] args) {
        String str = ";肖子凡";
        System.out.println(JSONObject.toJSON(str.trim().split(";")));
        String fName = " G:\\Java_Source\\navigation_tigra_menu\\demo1\\img\\000101_lev1arrow.gif ";

        String fileName = fName.trim().substring(fName.lastIndexOf("\\"));
        System.out.println("fileName = " + fileName);
        fileName = fileName.substring(0, fileName.lastIndexOf("_"));
        System.out.println("fileName = " + fileName);

    }
}
