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
 * <p>v2 章节控制器</p>
 */
@JMController.v2
@RestController("chapterControllerV2")
public class ChapterController {

    @Autowired
    private ApiClientFactory apiClientFactory;

    @GetMapping("/chapter/{id}")
    public Mono<String> chapterInfo(@PathVariable int id) {
        return apiClientFactory.chapter(id);
    }

    @GetMapping("/chapter/read/{id}")
    public Mono<String> read(@PathVariable int id) {
        return apiClientFactory.read(id);
    }
    @GetMapping("/chapter/{id}/comment/{page}")
    public Mono<String> comment(@PathVariable int id,
                                @PathVariable int page,
                                @RequestParam(value = "mode",defaultValue = "",required = false) String mode) {
        return apiClientFactory.chapterComment(mode, id, page);
    }
}
