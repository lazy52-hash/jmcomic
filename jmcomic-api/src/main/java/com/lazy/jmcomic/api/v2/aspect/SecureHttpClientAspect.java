package com.lazy.jmcomic.api.v2.aspect;

import com.lazy.jmcomic.api.v2.config.properties.SettingProperties;
import com.lazy.jmcomic.api.v2.crypto.decrypt.ResponseDecryptor;
import com.lazy.jmcomic.api.v2.crypto.sign.SignatureGenerator;
import com.lazy.jmcomic.api.v2.pojo.ApiRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * @since 2026.3.14
 * @author lazy
 * <p>请求签名以及响应解密切面，被拦截方法须返回 Mono&lt;String&gt;</p>
 */
@Slf4j
@Aspect
@Component
public class SecureHttpClientAspect {

    @Autowired
    private SettingProperties properties;

    @Around("@annotation(com.lazy.jmcomic.api.v2.annotation.SecureApi)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("进入签名生成");
        Object[] args = joinPoint.getArgs();
        ApiRequest apiRequest = (ApiRequest) args[0];
        long unixSeconds = new Date().toInstant().getEpochSecond();
        // 生成签名，注入 token/tokenParam 到请求头
        SignatureGenerator.Sign sign = SignatureGenerator.generate(properties.token(), properties.version(), unixSeconds);
        apiRequest.headers().put("Token", sign.token());
        apiRequest.headers().put("Tokenparam", sign.tokenParam());
        log.debug("签名生成成功");
        // 执行被拦截方法（返回 Mono<String>）
        @SuppressWarnings("unchecked")
        Mono<String> result = (Mono<String>) joinPoint.proceed(args);
        // 在响应流中对密文做 AES 解密
        return result.mapNotNull(data -> {
            try {
                return ResponseDecryptor.decryptData(properties.token(), unixSeconds, data);
            } catch (Exception e) {
                System.out.println(data);
                throw new RuntimeException("响应解密失败", e);
            }
        });
    }
}
