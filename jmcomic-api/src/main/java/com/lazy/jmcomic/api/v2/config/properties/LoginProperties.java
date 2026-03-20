package com.lazy.jmcomic.api.v2.config.properties;

import com.lazy.jmcomic.api.v2.constant.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(Properties.LOGIN)
public record LoginProperties(String username, String password) {
}
