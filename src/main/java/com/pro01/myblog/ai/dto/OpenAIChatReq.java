package com.pro01.myblog.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Optional;

@Data
public class OpenAIChatReq {
    private String model;
    private List<Message> messages;
    private Double temperature;   // 可选
    private Integer max_tokens;   // 可选

    @Data
    public static class Message {
        private String role;     // "system" | "user" | "assistant"
        private String content;
        public Message() {}
        public Message(String role, String content) { this.role = role; this.content = content; }
    }
}