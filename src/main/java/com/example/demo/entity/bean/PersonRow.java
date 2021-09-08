package com.example.demo.entity.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 发明人数据
 */
@Data
@Builder
public class PersonRow {
    /**
     * 发明人
     **/
    String name;
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
