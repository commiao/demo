package com.example.demo.entity.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 公司数据
 */
@Builder
@Data
public class CompanyRow {
    String companyNo;
    String name;
    Integer year;
    Integer totalCount;
    Integer useCountTotal;
    Integer aaCount;
    Integer bbCount;
    Integer ccCount;
}
