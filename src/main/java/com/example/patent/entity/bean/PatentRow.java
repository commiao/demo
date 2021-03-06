package com.example.patent.entity.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 专利数据
 */
@Data
@Builder
public class PatentRow {
    /**
     * 发明人
     **/
    String names;
    /**
     * 申请年份 yyyy
     **/
    Integer year;
    /**
     * 专利类型
     **/
    String type;
    /**
     * 申请号
     **/
    String applyNo;
    /**
     * 工商上市代码
     **/
    String companyNo;
    /**
     * 被引用次数
     **/
    Integer useCount;
}
