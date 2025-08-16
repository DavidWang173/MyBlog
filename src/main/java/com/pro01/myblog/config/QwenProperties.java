package com.pro01.myblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "qwen")
public class QwenProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private int timeoutMs;
    private int maxTokens;
}