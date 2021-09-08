package com.example.demo.entity.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountDTO {
    /**
     * 发明人
     **/
    String inventor;
    /**
     * 发明总数
     **/
    Integer patentCount;
    /**
     * 发明申请
     **/
    Integer inventionCount;
    /**
     * 实用新型
     **/
    Integer utilityModelCount;
    /**
     * 外观设计
     **/
    Integer designCount;
    /**
     * 引用次数
     **/
    Integer useCount;
}
