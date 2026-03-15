package com.lazy.jmcomic.gateway.controller.v2;

import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 小说网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController("novelControllerV2")
@RequestMapping("/v2/novel")
public class NovelController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/search")
    public Mono<String> search(@RequestParam String keyword) {
        log.debug("v2小说搜索: keyword={}", keyword);
        return apiClient.novelSearchV2(keyword);
    }

    @GetMapping("/list")
    public Mono<String> list(
            @RequestParam(required = false, defaultValue = "") String o,
            @RequestParam(required = false, defaultValue = "") String t,
            @RequestParam(required = false) Integer id) {
        log.debug("v2小说列表: o={}, t={}, id={}", o, t, id);
        return apiClient.novelListV2(o, t, id);
    }

    @GetMapping("/{nid}")
    public Mono<String> detail(@PathVariable int nid) {
        log.debug("v2小说详情: nid={}", nid);
        return apiClient.novelDetailV2(nid);
    }

    @GetMapping("/chapter/{cid}")
    public Mono<String> chapter(
            @PathVariable int cid,
            @RequestParam(required = false, defaultValue = "tw") String lang) {
        log.debug("v2小说章节: cid={}, lang={}", cid, lang);
        return apiClient.novelChapterV2(cid, lang);
    }
}
