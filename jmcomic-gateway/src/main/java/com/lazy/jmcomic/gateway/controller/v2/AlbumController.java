package com.lazy.jmcomic.gateway.controller.v2;

import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 漫画网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController("albumControllerV2")
@RequestMapping("/v2/album")
public class AlbumController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/search")
    public Mono<String> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page) {
        log.debug("v2搜索漫画: keyword={}, page={}", keyword, page);
        return apiClient.searchAlbumV2(keyword, page);
    }

    @GetMapping("/{id}")
    public Mono<String> detail(@PathVariable int id) {
        log.debug("v2漫画详情: id={}", id);
        return apiClient.getAlbumDetailV2(id);
    }

    @GetMapping("/hot-tags")
    public Mono<String> hotTags() {
        log.debug("v2热门话题");
        return apiClient.hotTagsV2();
    }

    @GetMapping("/random-recommend")
    public Mono<String> randomRecommend() {
        log.debug("v2随机推荐");
        return apiClient.randomRecommendV2();
    }
}
