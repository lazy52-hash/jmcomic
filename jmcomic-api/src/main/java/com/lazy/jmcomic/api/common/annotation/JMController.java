package com.lazy.jmcomic.api.common.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一控制器前缀
 */
public @interface JMController {
    @RestController
    @CrossOrigin
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @RequestMapping("/jmapi/v1")
    @interface v1{}
    @RestController
    @CrossOrigin
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @RequestMapping("/jmapi/v2")
    @interface v2{}
}
