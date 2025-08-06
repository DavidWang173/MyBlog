package com.pro01.myblog.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOperation {
    private Long id;
    private Long userId;
    private String operationType;
    private String targetType;
    private Long targetId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime operationTime;
}