package com.lazy.jmcomic.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * @author lazy
 * @since 2026.3.16
 * <p>控制层全局异常处理器（处理控制器 Mono 链中传播的异常）</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 解析微服务返回 500 时的处理
     */
    @ExceptionHandler(WebClientResponseException.InternalServerError.class)
    public ResponseEntity<Map<String, Object>> handleApiServerError(WebClientResponseException.InternalServerError ex) {
        log.error("解析微服务返回500: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("code", 502, "message", "解析服务异常，请稍后重试"));
    }
    @ExceptionHandler(WebClientResponseException.ServiceUnavailable.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(WebClientResponseException.ServiceUnavailable ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("code", 503, "message", "解析服务器连接失败,请联系站点维护者"));
    }
}