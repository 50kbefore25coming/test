package com.TTCS26.financeTracker.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration file khai báo các Bean được Spring tạo sẵn
 * Dùng RestTemplate để gọi HTTP request đến API ngoài
 */
@Configuration
public class RestClientConfig {

    /**
     * Tạo 1 Bean RestTemplate duy nhất (singleton)
     * Spring sẽ inject bean này vào mọi @Service, @Controller cần
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(5))
                .setReadTimeout(java.time.Duration.ofSeconds(5))
                .build();
    }
}