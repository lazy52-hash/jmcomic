package com.lazy.jmcomic.api.v1.client;

import com.lazy.jmcomic.api.v1.config.SiteRegistry;
import com.lazy.jmcomic.api.v1.config.properties.ImageProperties;
import com.lazy.jmcomic.api.v1.constant.Website;
import com.lazy.jmcomic.api.v1.task.ImageHttpPingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>图片下载客户端，轮询可用CDN列表进行下载，失败自动切换下一CDN重试</p>
 */
@Slf4j
@Component
public class ImageDownloadClient {
    @Autowired
    private SiteRegistry siteRegistry;
    @Autowired
    private ImageProperties imageProperties;
    @Autowired
    private ImageHttpPingTask pingTask;
    @Autowired
    private WebClient webClient;

    /** 轮询游标 */
    private final AtomicInteger cursor = new AtomicInteger(0);

    /**
     * 轮询获取下一个可用CDN域名
     * @return CDN域名，无可用CDN时返回null
     */
    private String nextCdn() {
        CopyOnWriteArrayList<String> cdns = siteRegistry.getAliveImageCdns();
        if (cdns.isEmpty()) {
            log.warn("无可用图片CDN");
            return null;
        }
        // 取绝对值防止溢出为负数
        int idx = Math.abs(cursor.getAndIncrement() % cdns.size());
        String cdn = cdns.get(idx);
        log.debug("分配图片CDN[{}]: {}", idx, cdn);
        return cdn;
    }

    /**
     * 下载图片，自动轮询CDN并重试
     * @param path 图片路径（如 /media/photos/123456/00001.webp）
     * @return 图片字节数组
     */
    public Mono<byte[]> download(String path) {
        int maxRetry = imageProperties.maxRetry();
        return tryDownload(path, 0, maxRetry);
    }

    /**
     * 尝试从当前CDN下载，失败则切换下一CDN递归重试
     * @param path 图片路径
     * @param attempt 当前重试次数
     * @param maxRetry 最大重试次数
     * @return 图片字节数组
     */
    private Mono<byte[]> tryDownload(String path, int attempt, int maxRetry) {
        if (attempt > maxRetry) {
            log.error("已达图片下载最大重试次数({}), 路径: {}", maxRetry, path);
            return Mono.error(new RuntimeException("图片下载失败：所有CDN重试已耗尽, path=" + path));
        }
        String cdn = nextCdn();
        if (cdn == null) {
            return Mono.error(new RuntimeException("图片下载失败：无可用CDN"));
        }
        String url = Website.PROT + cdn + path;
        int timeout = imageProperties.connection().timeout();
        log.debug("下载图片: {} (第{}次尝试)", url, attempt + 1);
        return webClient.get()
                .uri(url)
                .header("Host", cdn)
                .header("Referer", Website.PROT + cdn)
                .headers(h -> h.setAll(Website.HEADERS))
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(e -> {
                    log.warn("CDN {} 下载图片失败(第{}次): {}", cdn, attempt + 1, e.getMessage());
                    // 触发CDN连通性检查
                    pingTask.ping(cdn);
                })
                .onErrorResume(e -> tryDownload(path, attempt + 1, maxRetry));
    }
}