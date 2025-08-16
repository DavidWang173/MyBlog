package com.pro01.myblog.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(QwenProperties.class)
public class QwenClientConfig {

    @Bean
    public WebClient qwenWebClient(QwenProperties props) {
        // 如果没有配置 API Key，不抛异常，只提示
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            System.out.println("⚠️ 警告: 未配置 DASHSCOPE_API_KEY，AI 摘要接口将不可用");
        }

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getTimeoutMs())
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(props.getTimeoutMs() / 1000));
                    conn.addHandlerLast(new WriteTimeoutHandler(props.getTimeoutMs() / 1000));
                });

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiKey()) // 即使为空也能构造
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}