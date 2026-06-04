package com.Peter.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "contract")//自动读取ymal里的配置
/**
 * 配置文件读取
 */
public class ContractConfig {
    private String title;
    private String version;
    private String updateTime;
    private String content;
}
