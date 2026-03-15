package com.lazy.jmcomic.api.v1.client;

import com.lazy.jmcomic.api.v1.config.SiteRegistry;
import com.lazy.jmcomic.api.v1.config.properties.WebsiteProperties;
import com.lazy.jmcomic.api.v1.task.WebsiteHttpingTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author lazy
 * @since 2026.3.9
 * <p>爬取客户端工厂，管理站点分配、占用状态和故障重试</p>
 * <p>顺序轮询站点列表，单站点不能并发爬取，轮询到末尾统一释放</p>
 */
@Slf4j
@Component
public class WebCrawlFactory {
    @Autowired
    private SiteRegistry siteRegistry;
    @Autowired
    private WebsiteProperties websiteProperties;
    @Autowired
    private WebsiteHttpingTask httpingTask;
    @Autowired
    private Proxy proxy;
    @Autowired
    private ConcurrentHashMap<String, String> cookies;
    /** 可用站点列表，持有SiteRegistry内部引用，ping后自动更新 */
    private CopyOnWriteArrayList<String> aliveWebsites;
    /** 每个站点对应的客户端实例 */
    private final ConcurrentHashMap<String, WebCrawlClient> clients = new ConcurrentHashMap<>();
    /** 轮询游标 */
    private final AtomicInteger cursor = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        this.aliveWebsites = siteRegistry.getAliveWebsites();
    }

    /**
     * 顺序获取下一个站点，到达末尾时释放所有占用并重置游标
     */
    private synchronized String nextSite() {
        int size = aliveWebsites.size();
        if (size == 0) {
            log.warn("无可用站点");
            return null;
        }
        int idx = cursor.get();
        if (idx >= size) {
            releaseAll();
            idx = 0;
        }
        String domain = aliveWebsites.get(idx);
        cursor.set(idx + 1);
        log.debug("分配站点[{}]: {}", idx, domain);
        return domain;
    }

    private void releaseAll() {
        cursor.set(0);
        log.debug("轮询完毕，游标重置");
    }

    private WebCrawlClient getClient(String domain) {
        return clients.computeIfAbsent(domain, d ->
                new WebCrawlClient(d, websiteProperties, proxy, cookies, httpingTask));
    }

    /**
     * 执行爬取任务，顺序轮询站点，失败切换下一站点重试
     * @param action 接收WebCrawlClient，返回Document的操作
     * @return Document结果，全部重试失败返回null
     */
    public Connection.Response execute(Function<WebCrawlClient, Connection.Response> action) {
        int maxRetry = websiteProperties.maxRetry();
        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            String site = nextSite();
            if (site == null) return null;
            WebCrawlClient client = getClient(site);
            Connection.Response result = action.apply(client);
            if (result != null) {
                return result;
            }
            log.warn("站点 {} 爬取失败 (第{}次)", site, attempt + 1);
            //已在爬取站点对象设置ping
            //httpingTask.ping(site);
        }
        log.warn("已达最大重试次数({}), 爬取失败", maxRetry);
        return null;
    }
}