package com.lazy.jmcomic.api.v1.config;

import com.lazy.jmcomic.api.v1.config.properties.DevProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2026.3.10
 * 代理配置
 */
@Slf4j
@Configuration
public class DevConfig {
    @Autowired
    private DevProperties properties;
    @Autowired
    private ConcurrentHashMap<String, String> cookies;
    @PostConstruct
    public void devInit(){
        if(!properties.isEmpty()){
            log.info("检测到开发配置,已阻止默认初始化逻辑,准备加载开发配置");
            if(!properties.cookies().isEmpty()){
                cookies.putAll(properties.cookies());
            }
        }
    }
    @Bean
    public java.net.Proxy proxy(){
        if(properties.isEmpty()) return Proxy.NO_PROXY;
        if(properties.proxy().enabled()){
            log.info("开发环境启用代理配置-{}:{}",properties.proxy().host(),properties.proxy().port());
            return new java.net.Proxy(
                    java.net.Proxy.Type.HTTP,
                    new InetSocketAddress(properties.proxy().host(),properties.proxy().port()));
        }else{
            return java.net.Proxy.NO_PROXY;
        }
    }
}
