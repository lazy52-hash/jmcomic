package com.lazy.jmcomic.gateway.controller.v2;

import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 章节网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController("chapterControllerV2")
@RequestMapping("/v2/chapter")
public class ChapterController {

    @Autowired
    private ApiClient apiClient;

    @GetMapping("/{id}")
    public Mono<String> chapterInfo(@PathVariable int id) {
        log.debug("v2章节信息: id={}", id);
        return apiClient.getChapterInfoV2(id);
    }

    @GetMapping("/read/{id}")
    public Mono<String> read(@PathVariable int id) {
        log.debug("v2章节阅读: id={}", id);
        return apiClient.readV2(id);
    }

    @GetMapping("/{id}/comment/{page}")
    public Mono<String> comment(@PathVariable int id,
                                @PathVariable int page,
                                @RequestParam(value = "mode", defaultValue = "", required = false) String mode) {
        log.debug("v2章节评论: id={}, page={}, mode={}", id, page, mode);
        return apiClient.chapterCommentV2(id, page, mode);
    }
}
