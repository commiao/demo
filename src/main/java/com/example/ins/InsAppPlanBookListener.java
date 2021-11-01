package com.example.ins;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * excel监听：公司数据
 *
 * @author 井淼
 */
@Slf4j
public class InsAppPlanBookListener extends AnalysisEventListener<InsAppPlanBook> {

    private List<InsAppPlanBook> bookList = new ArrayList<>();

    @Override
    public void invoke(InsAppPlanBook importData, AnalysisContext analysisContext) {
//        log.debug("解析到一条数据:{}", JSONObject.toJSONString(importData));
        bookList.add(importData);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

    public List<InsAppPlanBook> getBookList() {
        return bookList;
    }

}
