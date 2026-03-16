package com.lazy.jmcomic.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author lazy
 * @since 2026.3.16
 * <p>路由不存在（404）全局处理器。
 * WebFlux 中未匹配路由的 404 发生在 DispatcherHandler 之前，
 * 无法被 @ControllerAdvice 捕获，需通过 WebExceptionHandler 处理。
 * Order(-2) 优先于 Spring Boot 默认的 DefaultErrorWebExceptionHandler(-1)。</p>
 */
@Slf4j
@Order(-2)
@Component
public class NotFoundWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public NotFoundWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof ResponseStatusException rse
                && HttpStatus.NOT_FOUND.equals(rse.getStatusCode())) {
            String path = exchange.getRequest().getPath().value();
            log.warn("请求地址不存在: {}", path);
            return writeJson(exchange, HttpStatus.NOT_FOUND,
                    Map.of("code", 404, "message", "请求的地址不存在: " + path));
        }
        return Mono.error(ex);
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, Map<String, Object> body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}