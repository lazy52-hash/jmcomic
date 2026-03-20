package com.lazy.jmcomic.api.v2.client;

import com.alibaba.fastjson.JSON;
import com.lazy.jmcomic.api.v2.pojo.ApiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 API 单域名请求客户端（非 Spring Bean，由 ApiClientFactory 手动创建并注入依赖）</p>
 * <p>使用 WebClient 发起请求，单站点无速率限制锁，返回响应式 Mono</p>
 */
@Slf4j
public final class ApiClient {

    private final String baseUrl;
    private final WebClient webClient;
    private final int maxRetry;
    private final Duration timeout;

    ApiClient(String domain, WebClient webClient, int maxRetry, int timeoutMs) {
        this.baseUrl = "https://" + domain + "/";
        this.webClient = webClient;
        this.maxRetry = maxRetry;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    /**
     * 执行 HTTP 请求，返回原始响应体 Mono（加密，由切面负责解密）
     * @param request 请求封装对象（headers 已由 @SecureApi 切面注入签名）
     * @return 原始响应体 Mono；非 2xx 或网络异常时返回 Mono.empty()
     */
    Mono<String> doRequest(ApiRequest request) {
        String url = baseUrl + request.path();
        log.debug("API请求: {} {}", request.method(), url);

        WebClient.RequestBodySpec spec = webClient
                .method(toHttpMethod(request.method()))
                .uri(uri -> {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
                    // 将 params 追加为查询参数
                    if (request.params() != null) {
                        request.params().forEach(builder::queryParam);
                    }
                    return builder.build().toUri();
                })
                .headers(h -> {
                    if (request.headers() != null) {
                        request.headers().forEach(h::set);
                    }
                });
        spec.contentType(MediaType.APPLICATION_JSON);
        // 使用 exchangeToMono 获取完整响应，便于记录状态码
        Mono<String> mono = buildExchange(spec, request)
                .timeout(timeout)
                .retry(maxRetry);

        return mono.doOnError(e -> log.warn("API请求网络异常: {} -> {} {}", url,
                        e.getClass().getSimpleName(), e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<String> buildExchange(WebClient.RequestBodySpec spec, ApiRequest request) {
        WebClient.RequestHeadersSpec<?> headersSpec =
                (request.method() == ApiRequest.Method.POST && request.data() != null)
                        ? spec.bodyValue(request.data())
                        : spec;
        return headersSpec.exchangeToMono(response -> {
            log.debug("API响应状态: {}", response.statusCode());
            if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(String.class);
            }
            log.warn("API响应非2xx: {}", response.statusCode());
            return response.releaseBody().then(Mono.empty());
        });
    }

    private HttpMethod toHttpMethod(ApiRequest.Method method) {
        return method == ApiRequest.Method.POST ? HttpMethod.POST : HttpMethod.GET;
    }
}
