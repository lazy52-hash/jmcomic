package com.lazy.jmcomic.api.v2.config;

import com.lazy.jmcomic.api.v2.pojo.LoginInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
@Configuration
public class LoginInfoBean {
    @Getter
    @Setter
    private volatile LoginInfo loginInfo;
}
