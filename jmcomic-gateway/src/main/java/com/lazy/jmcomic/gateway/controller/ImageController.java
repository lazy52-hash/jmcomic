package com.lazy.jmcomic.gateway.controller;

import com.lazy.jmcomic.common.dto.ChapterImageDto;
import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>图片网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/image")
public class ImageController {
    @Autowired
    private ApiClient apiClient;

    /**
     * 获取用户头像
     * @param filename 头像文件名（含后缀，如 abc123.jpg）
     * @return 头像图片
     */
    @GetMapping("/avatar/{filename:.+}")
    public Mono<ResponseEntity<byte[]>> avatar(@PathVariable String filename) {
        log.debug("网关获取用户头像: filename={}", filename);
        return apiClient.getUserAvatar(filename);
    }

    /**
     * 获取漫画封面
     * @param filename 漫画ID
     * @return 封面图片
     */
    @GetMapping("/cover/{filename}")
    public Mono<ResponseEntity<byte[]>> cover(@PathVariable String filename) {
        log.debug("网关获取封面: {}", filename);
        return apiClient.getAlbumCover(filename);
    }

    /**
     * 下载GIF图片（不加密，直接透传）
     * @param chapterId 章节ID
     * @param scrambleId scrambleId（GIF不使用，保持参数一致）
     * @param fileName 文件名（无后缀）
     * @return GIF图片
     */
    @GetMapping("/chapter/{chapterId}/{scrambleId}/{fileName}.gif")
    public Mono<ResponseEntity<byte[]>> gif(
            @PathVariable int chapterId,
            @PathVariable int scrambleId,
            @PathVariable String fileName) {
        log.debug("网关下载GIF: chapterId={}, fileName={}", chapterId, fileName);
        return apiClient.getGifImage(chapterId, fileName);
    }

    /**
     * 解码章节图片
     * @param chapterId 章节ID
     * @param scrambleId scrambleId
     * @param fileName 文件名（无后缀）
     * @return 解码后的图片
     */
    @GetMapping("/chapter/{chapterId}/{scrambleId}/{fileName}.webp")
    public Mono<ResponseEntity<byte[]>> decode(
            @PathVariable int chapterId,
            @PathVariable int scrambleId,
            @PathVariable String fileName) {
        log.debug("网关解码图片: chapterId={}, scrambleId={}, fileName={}", chapterId, scrambleId, fileName);
        return apiClient.decodeImage(new ChapterImageDto(fileName, scrambleId, chapterId));
    }
}