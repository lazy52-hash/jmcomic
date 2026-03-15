package com.lazy.jmcomic.api.v2.config.properties;

import com.lazy.jmcomic.api.v2.constant.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * v2 API 配置：token、version 用于生成请求签名及响应解密，httping 用于域名连通性探测
 * @param token   客户端签名 token
 * @param version 客户端版本
 * @param httping 域名连通性探测配置
 */
@ConfigurationProperties(Properties.SETTING)
public record SettingProperties(
        String token,
        String version,
        Httping httping,
        Api api
) {
    /**
     * @param path     探测路径（相对路径，默认 setting 接口）
     * @param timeout  连接超时（ms）
     * @param interval 定时探测间隔
     */
    public record Httping(
            @DefaultValue("setting") String path,
            @DefaultValue("5000") int timeout,
            @DefaultValue("PT30M") Duration interval
    ) {}
    public record Api(
            @DefaultValue("3") int maxRetry,
            @DefaultValue("5000") int timeout
    ) {}
}
