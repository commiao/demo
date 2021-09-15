package com.example.personCity.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.Utils;
import com.example.personCity.entity.ExcelCity;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * excel监听：城市迁移数据
 *
 * @author 井淼
 */
@Slf4j
public class PersonCityListener extends AnalysisEventListener<ExcelCity> {
    private List<ExcelCity> personCityList = new ArrayList<>();

    @Override
    public void invoke(ExcelCity importData, AnalysisContext analysisContext) {
//        log.debug("解析到一条数据:{}", JSONObject.toJSONString(importData));
        if (!Utils.checkObjAllFieldsIsNull(importData)) {
            personCityList.add(importData);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

    public List<ExcelCity> getPersonCityList() {
        return personCityList;
    }

}
