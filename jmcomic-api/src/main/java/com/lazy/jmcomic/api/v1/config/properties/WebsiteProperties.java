package com.lazy.jmcomic.api.v1.config.properties;

import com.lazy.jmcomic.api.v1.constant.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p>web爬取配置</p>
 * @param connection 连接配置
 * @param httping 连通性测试
 * @param rateLimit 单站点限流
 * @param maxRetry 最大重试次数
 */
@ConfigurationProperties(Properties.WEBSITE)
public record WebsiteProperties(
        Connection connection,
        Httping httping,
        RateLimit rateLimit,
        @DefaultValue("3") int maxRetry
) {
    /**
     *
     * @param timeout 超时时间
     * @param readTimeout 读超时时间
     */
    public record Connection(
            @DefaultValue("5000") int timeout,
            @DefaultValue("5000") int readTimeout) {}

    /**
     *
     * @param headers 自定义请求头
     * @param timeout 连接超时
     * @param interval 间隔
     * @param path 测试地址
     */
    public record Httping(
            Map<String,String> headers,
            @DefaultValue("5000") int timeout,
            @DefaultValue("PT30M") Duration interval,
            @DefaultValue("/ajax/user_daily_event") String path) {}
    public record RateLimit(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("30") int minute) {}

}