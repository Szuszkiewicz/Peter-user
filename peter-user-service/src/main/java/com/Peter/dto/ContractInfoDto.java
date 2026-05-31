package com.Peter.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractInfoDto {
    /**
     * 合同标题
     */
    private String title;
    /**
     * 合同版本
     */
    private String version;
    /**
     * 合同更新时间
     */
    private String updateTime;
    /**
     * 合同内容
     */
    private String content;
}
