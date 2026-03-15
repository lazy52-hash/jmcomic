package com.lazy.jmcomic.api.v1.config.properties;

import com.lazy.jmcomic.api.v1.constant.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p>解析图片配置</p>
 */
@ConfigurationProperties(Properties.IMAGE)
public record ImageProperties(
        Connection connection,
        Httping httping,
        Watermark watermark,
        @DefaultValue("3") int maxRetry){
    public record Watermark(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("@lazy_decodeNode") String value,
            @DefaultValue("true") boolean addDate){}
    public record Httping(
            Map<String,String> headers,
            @DefaultValue("5000") int timeout,
            @DefaultValue("/media/albums/350234_3x4.jpg") String path,
            @DefaultValue("PT30M") Duration interval){
    }
    public record Connection(
            @DefaultValue("5000") int timeout,
            @DefaultValue("5000") int readTimeout) {}
}
