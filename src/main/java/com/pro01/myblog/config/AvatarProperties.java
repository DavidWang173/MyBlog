package com.pro01.myblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "avatar")
public class AvatarProperties {
    private String uploadPath;
    private String accessUrlPrefix;
}