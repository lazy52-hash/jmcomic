package com.lazy.jmcomic.api.v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * <p>WebClient 配置</p>
 * <p>目前是图片服务为了高并发使用的 WebFlux，其他地方依旧传统写法</p>
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // 启用 301/302 自动重定向（用 followRedirect(true) 走内部实现，BiPredicate 版本在部分 Reactor Netty 版本中读不到 Location 头）
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true);

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024))
                .build();
    }
}
