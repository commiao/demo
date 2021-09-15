package com.example.person.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.Utils;
import com.example.person.entity.excelBean.ExcelPatent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * excel监听：个人数据
 *
 * @author 井淼
 */
@Slf4j
public class PersonPatentListener extends AnalysisEventListener<ExcelPatent> {
    private List<ExcelPatent> personList = new ArrayList<>();

    @Override
    public void invoke(ExcelPatent importData, AnalysisContext analysisContext) {
//        log.debug("解析到一条数据:{}", JSONObject.toJSONString(importData));
        if (!Utils.checkObjAllFieldsIsNull(importData)) {
            personList.add(importData);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

    public List<ExcelPatent> getPersonList() {
        return personList;
    }

}
