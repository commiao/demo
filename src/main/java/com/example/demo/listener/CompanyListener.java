package com.example.demo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import com.example.Utils;
import com.example.demo.entity.importDTO.ImportCompanyData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * excel监听：公司数据
 *
 * @author 井淼
 */
@Slf4j
public class CompanyListener extends AnalysisEventListener<ImportCompanyData> {

    private List<ImportCompanyData> companyList = new ArrayList<>();

    @Override
    public void invoke(ImportCompanyData importData, AnalysisContext analysisContext) {
//        log.debug("解析到一条数据:{}", JSONObject.toJSONString(importData));
        if (!Utils.checkObjAllFieldsIsNull(importData)) {
            if (StringUtils.isBlank(importData.getId())) {
                log.info("解析到一条异常数据:{}", JSONObject.toJSONString(importData));
            } else {
                importData.setId(String.format("%0" + 6 + "d", Integer.valueOf(importData.getId())));
                if (StringUtils.isBlank(importData.getName())) {
                    importData.setName(importData.getShortName());
                }
                companyList.add(importData);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

    public List<ImportCompanyData> getCompanyList() {
        return companyList.stream().filter(distinctByKey(ImportCompanyData::getId)).collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return object -> seen.putIfAbsent(keyExtractor.apply(object), Boolean.TRUE) == null;
    }
}
