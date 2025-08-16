package com.pro01.myblog.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class OpenAIChatResp {
    private List<Choice> choices;
    @Data public static class Choice {
        private Message message;
    }
    @Data public static class Message {
        private String role;
        private String content;
    }
    public String firstContent() {
        return Optional.ofNullable(choices)
                .filter(c -> !c.isEmpty())
                .map(c -> c.get(0).getMessage().getContent())
                .orElse(null);
    }
}