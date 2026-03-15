package com.lazy.jmcomic.api.v2.controller;

import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v2.client.ApiClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 漫画控制器</p>
 */
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
                               @RequestParam(defaultValue = "1") int page) {
        return apiClientFactory.search(keyword, page);
    }
    @GetMapping("/album/download/{id}")
    public Mono<String> download(@PathVariable int id) {
        return apiClientFactory.downloadPage(id);
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
