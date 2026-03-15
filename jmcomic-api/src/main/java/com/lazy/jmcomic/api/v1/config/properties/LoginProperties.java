package com.lazy.jmcomic.api.v1.config.properties;

import com.lazy.jmcomic.api.v1.constant.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * <p>jm登录配置</p>
 * <p>由于web端需要登录才能获取所有漫画</p>
 * @param day 重新登录间隔日期
 * */
@Slf4j
@ConfigurationProperties(Properties.LOGIN)
public record LoginProperties(String username, String password,int reloginDay) {
    public LoginProperties{
        //log.info("初始化JM登录配置\tusername:{},password:{}", username, password.replaceAll("\\.","*"));
//        log.info("初始化JM登录配置\tusername:{},password:{}", username, password);
    }
}
