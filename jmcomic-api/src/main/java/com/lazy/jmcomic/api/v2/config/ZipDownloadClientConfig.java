package com.lazy.jmcomic.api.v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ZipDownloadClientConfig {
    /**
     * 500MB
     * @param webClientBuilder
     * @return
     */
    @Bean
    public WebClient zipDownloadClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(500 * 1024 * 1024))
                .build();
    }
}
