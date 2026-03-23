package com.lazy.jmcomic.api.v2.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v2.client.ApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 漫画控制器</p>
 */
@Slf4j
@JMController.v2
@RestController("albumControllerV2")
public class AlbumController {
    @Autowired
    private ApiClientFactory apiClientFactory;

    @GetMapping("/album/week")
    public Mono<String> week(){
        return apiClientFactory.week();
    }
    @GetMapping("/album/search")
    public Mono<String> search(@RequestParam String keyword,
                               @RequestParam(defaultValue = "1") int page) throws IOException {
        return apiClientFactory.search(keyword, page);
    }

    @GetMapping("/album/{id}")
    public Mono<String> albumDetail(@PathVariable int id) {
        return apiClientFactory.album(id);
    }

    @GetMapping("/album/hot-tags")
    public Mono<String> hotTags() {
        return apiClientFactory.hotTags();
    }

    @GetMapping("/album/random-recommend")
    public Mono<String> randomRecommend() {
        return apiClientFactory.randomRecommend();
    }
}
