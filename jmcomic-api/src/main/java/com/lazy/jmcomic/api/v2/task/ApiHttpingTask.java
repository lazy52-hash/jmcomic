package com.lazy.jmcomic.api.v2.task;

import com.lazy.jmcomic.api.common.util.HttpPingUtil;
import com.lazy.jmcomic.api.v2.config.ApiDomainRegistry;
import com.lazy.jmcomic.api.v2.config.properties.SettingProperties;
import com.lazy.jmcomic.api.v2.constant.ApiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>探测 v2 API 域名连通性任务，使用 setting 接口路径作为探测目标</p>
 */
@Slf4j
@Component
public class ApiHttpingTask {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private SettingProperties settingProperties;

    @Autowired
    private Proxy proxy;

    @Autowired
    private ApiDomainRegistry apiDomainRegistry;

    /**
     * 定时探测所有 API 域名连通性
     */
    @Scheduled(fixedRateString = "${jmcomic.v2.setting.httping.interval:PT30M}")
    public void run() {
        for (String domain : ApiRequest.DOMAINS) {
            ping(domain);
        }
    }

    /**
     * 异步探测指定域名，更新注册表状态
     * @param domain 域名
     */
    public void ping(String domain) {
        executorService.submit(() -> {
            try {
                // 随机延迟，避免同时发起大量探测
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SettingProperties.Httping httping = settingProperties.httping();
            String url = "https://" + domain + "/" + httping.path();
            boolean alive;
            try {
                alive = HttpPingUtil.isAlive(url, new HashMap<>(ApiRequest.HEADERS), proxy, httping.timeout());
            } catch (IOException e) {
                log.warn("API域名 {} httping IO异常: {}", domain, e.getMessage());
                alive = false;
            }
            apiDomainRegistry.setAlive(domain, alive);
        });
    }
}
