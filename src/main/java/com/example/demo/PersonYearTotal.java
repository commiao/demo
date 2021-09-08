package com.example.demo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonYearTotal {
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
     * 刚上上市代码
     **/
    String companyNo;
    /**
     * 被引用次数
     **/
    Integer useCountTotal;
    /**
     * 专利数量
     **/
    Integer countTotal;
}
