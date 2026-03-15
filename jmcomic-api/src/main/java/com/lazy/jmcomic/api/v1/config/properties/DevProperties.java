package com.lazy.jmcomic.api.v1.config.properties;

import com.lazy.jmcomic.api.v1.constant.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

/**
 * 若配置了此属性则默认开发环境,使用开发配置
 * @param cookies 开发环境下的cookies,避免重复登录
 * @param proxy 代理
 */
@ConfigurationProperties(Properties.DEV)
public record DevProperties(Map<String,String> cookies,Proxy proxy) {
    public record Proxy(Boolean enabled,
                        String host,
                        Integer port) {
    }
    public boolean isEmpty(){
        return  cookies==null || cookies.isEmpty()|| proxy().enabled==null||proxy().host==null||proxy().port==null;
    }
}
