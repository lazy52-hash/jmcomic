package com.lazy.jmcomic.gateway.controller;

import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.pojo.ChapterInfo;
import com.lazy.jmcomic.common.pojo.Comment;
import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>章节网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/chapter")
public class ChapterController {
    @Autowired
    private ApiClient apiClient;

    /**
     * 获取章节信息
     * @param id 章节ID
     * @return 章节信息
     */
    @GetMapping("/{id}")
    public Mono<ChapterInfo> chapterInfo(@PathVariable int id) {
        log.debug("网关获取章节信息: id={}", id);
        return apiClient.getChapterInfo(id);
    }

    /**
     * 查询章节评论
     * @param id 漫画/章节ID
     * @param page 评论页码
     * @return 评论列表
     */
    @GetMapping("/{id}/comment/{page}")
    public Mono<List<Comment>> comment(
            @PathVariable int id,
            @PathVariable int page) {
        log.debug("网关查询评论: videoId={}, page={}", id, page);
        return apiClient.getComments(new CommentQueryDataDto(id, null, page));
    }
}