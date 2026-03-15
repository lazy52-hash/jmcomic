package com.lazy.jmcomic.api.v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * cookies容器
 */
@Configuration
public class SiteCookie {
    private final ConcurrentHashMap<String, String> cookies = new ConcurrentHashMap<>();
    @Bean
    public ConcurrentHashMap<String, String> cookies() {
        return cookies;
    }
}
