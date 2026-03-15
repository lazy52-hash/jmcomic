package com.lazy.jmcomic.api.v1.task;

import com.lazy.jmcomic.api.v1.config.SiteRegistry;
import com.lazy.jmcomic.api.v1.config.properties.ImageProperties;
import com.lazy.jmcomic.api.v1.constant.Website;
import com.lazy.jmcomic.api.common.util.HttpPingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lazy
 * @since 2026.3.8
 * <p>探测图片CDN站点连通性任务</p>
 */
@Slf4j
@Component
public class ImageHttpPingTask {
    private final ExecutorService executorService=Executors.newFixedThreadPool(4);
    @Autowired
    private ImageProperties imageProperties;
    @Autowired
    private Proxy proxy;
    @Autowired
    private SiteRegistry siteRegistry;

    @Scheduled(fixedRateString = "${jmcomic.v1.image.httping.interval:PT30M}")
    public void run() {
        for (String cdn : Website.IMAGES_CDN_ARRAY) {
            for (String siteDomain : Website.DOMAINS) {
                ping(cdn+"."+siteDomain);
            }
        }
    }

    public void ping(String cdnDomain) {
        executorService.submit(()->{
            ImageProperties.Httping httping=imageProperties.httping();
            String baseurl=Website.PROT+cdnDomain;
            Map<String,String> headers=HttpPingUtil.resolveHeaders(httping.headers(),cdnDomain,baseurl);
            boolean alive= false;
            try {
                alive = HttpPingUtil.isAlive(
                        baseurl+httping.path(),
                        headers,
                        proxy,
                        httping.timeout());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            siteRegistry.setImageCdnAlive(cdnDomain,alive);
        });
    }
}
