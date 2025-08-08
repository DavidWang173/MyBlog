package com.pro01.myblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cover")
public class CoverProperties {
    private String uploadPath;
    private String accessUrlPrefix;
}