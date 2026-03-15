package com.lazy.jmcomic.api.v1.config;

import com.lazy.jmcomic.api.v1.constant.Website;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>站点可用性注册表，维护网站和图片CDN的启用状态</p>
 * <p>aliveWebsites/aliveImageCdns 为实时更新的引用列表，外部持有引用即可感知变化</p>
 */
@Slf4j
@Component
public class SiteRegistry {
    private final ConcurrentHashMap<String, Boolean> websiteStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> imageCdnStatus = new ConcurrentHashMap<>();
    @Getter
    private final CopyOnWriteArrayList<String> aliveWebsites = new CopyOnWriteArrayList<>();
    @Getter
    private final CopyOnWriteArrayList<String> aliveImageCdns = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        for (String domain : Website.DOMAINS) {
            websiteStatus.put(domain, true);
        }
        for (String cdn : Website.IMAGES_CDN_ARRAY) {
            for (String domain : Website.DOMAINS) {
                imageCdnStatus.put(cdn + "." + domain, true);
            }
        }
        rebuildAliveWebsites();
        rebuildAliveImageCdns();
    }

    public void setWebsiteAlive(String domain, boolean alive) {
        websiteStatus.put(domain, alive);
        rebuildAliveWebsites();
        log.info("站点 {} 状态: {}", domain, alive ? "启用" : "禁用");
    }

    public void setImageCdnAlive(String cdnDomain, boolean alive) {
        imageCdnStatus.put(cdnDomain, alive);
        rebuildAliveImageCdns();
        log.info("图片CDN {} 状态: {}", cdnDomain, alive ? "启用" : "禁用");
    }

    public boolean isWebsiteAlive(String domain) {
        return websiteStatus.getOrDefault(domain, false);
    }

    public boolean isImageCdnAlive(String cdnDomain) {
        return imageCdnStatus.getOrDefault(cdnDomain, false);
    }

    private void rebuildAliveWebsites() {
        List<String> alive = websiteStatus.entrySet().stream()
                .filter(e -> e.getValue()).map(e -> e.getKey()).toList();
        aliveWebsites.clear();
        aliveWebsites.addAll(alive);
    }

    private void rebuildAliveImageCdns() {
        List<String> alive = imageCdnStatus.entrySet().stream()
                .filter(e -> e.getValue()).map(e -> e.getKey()).toList();
        aliveImageCdns.clear();
        aliveImageCdns.addAll(alive);
    }
}