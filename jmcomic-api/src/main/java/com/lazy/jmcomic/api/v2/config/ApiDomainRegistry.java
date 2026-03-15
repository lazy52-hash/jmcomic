package com.lazy.jmcomic.api.v2.config;

import com.lazy.jmcomic.api.v2.constant.ApiRequest;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 API 域名可用性注册表</p>
 * <p>aliveDomains 为实时更新的引用列表，外部持有引用即可感知变化</p>
 */
@Slf4j
@Component
public class ApiDomainRegistry {

    private final ConcurrentHashMap<String, Boolean> domainStatus = new ConcurrentHashMap<>();

    @Getter
    private final CopyOnWriteArrayList<String> aliveDomains = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        // 初始化时所有域名默认可用
        for (String domain : ApiRequest.DOMAINS) {
            domainStatus.put(domain, true);
        }
        rebuild();
    }

    /**
     * 更新域名可用状态，并刷新 aliveDomains 列表
     * @param domain 域名
     * @param alive  是否可用
     */
    public void setAlive(String domain, boolean alive) {
        domainStatus.put(domain, alive);
        rebuild();
        log.info("API域名 {} 状态: {}", domain, alive ? "启用" : "禁用");
    }

    public boolean isAlive(String domain) {
        return domainStatus.getOrDefault(domain, false);
    }

    private void rebuild() {
        List<String> alive = domainStatus.entrySet().stream()
                .filter(e -> e.getValue())
                .map(e -> e.getKey())
                .toList();
        aliveDomains.clear();
        aliveDomains.addAll(alive);
    }
}
