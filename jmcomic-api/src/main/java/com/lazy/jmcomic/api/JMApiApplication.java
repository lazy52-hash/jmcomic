package com.lazy.jmcomic.api;

import com.lazy.jmcomic.api.v1.config.properties.*;

import com.lazy.jmcomic.api.v2.config.properties.SettingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
@Slf4j
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties({
                DevProperties.class,
                LoginProperties.class,
                WebsiteProperties.class,
                ImageProperties.class,
                SettingProperties.class
                //ProxyProperties.class,
        })
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class JMApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(JMApiApplication.class, args);
        log.info("节点运行成功");
    }
}
