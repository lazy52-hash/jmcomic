package com.lazy.jmcomic.api.v2.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lazy.jmcomic.api.v2.client.ApiClientFactory;
import com.lazy.jmcomic.api.v2.config.LoginInfoBean;
import com.lazy.jmcomic.api.v2.config.properties.LoginProperties;
import com.lazy.jmcomic.api.v2.pojo.LoginInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LoginTask {
    @Autowired
    private LoginProperties properties;
    @Autowired
    private ApiClientFactory factory;
    @Autowired
    private LoginInfoBean loginInfoBean;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    @PostConstruct
    @Scheduled(cron = "0 0 9 * * Mon")
    public void scheduleLogin() {
        log.info("v2定时登录任务开始");
        loginWithRetry()
                .doOnSubscribe(s -> retryCount.set(0))
                .subscribe(
                        loginInfo -> {
                            log.info("v2登录成功: {}", loginInfo.username());
                            loginInfoBean.setLoginInfo(loginInfo);
                        },
                        error -> {
                            log.error("v2登录最终失败", error);
                        }
                );
    }

    private Mono<LoginInfo> loginWithRetry() {
        return factory.login(properties.username(), properties.password())
                .flatMap(this::parseLoginResponse)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30))
                        .doBeforeRetry(retrySignal -> {
                            long attempt = retrySignal.totalRetries() + 1;
                            retryCount.set((int) attempt);
                            log.warn("v2登录第{}次重试", attempt);
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("v2登录重试{}次后失败", retrySignal.totalRetries());
                            return new RuntimeException("登录失败，已达最大重试次数");
                        }))
                .doOnSuccess(info -> {
                    if (retryCount.get() > 0) {
                        log.info("v2登录成功，共重试{}次", retryCount.get());
                    }
                });
    }

    private Mono<LoginInfo> parseLoginResponse(String response) {
        return Mono.fromCallable(() -> {
            if (response == null || response.trim().isEmpty()) {
                throw new IllegalArgumentException("登录响应为空");
            }
            return JSON.parseObject(response, LoginInfo.class);
        });
    }
}
