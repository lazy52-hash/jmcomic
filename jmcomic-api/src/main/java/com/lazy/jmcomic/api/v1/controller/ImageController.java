package com.lazy.jmcomic.api.v1.controller;

import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v1.service.ImageService;
import com.lazy.jmcomic.common.dto.ChapterImageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import java.io.IOException;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>图片控制器</p>
 */
@Slf4j
@JMController.v1
public class ImageController {
    @Autowired
    private ImageService imageService;
    @PostMapping("/image/decode")
    public Mono<ResponseEntity<byte[]>> decode(@RequestBody ChapterImageDto chapterImage) {
        log.debug("开始解析图片-scrambleId：{},chapterId:{},fileName:{}",
                chapterImage.scrambleId(), chapterImage.chapterId(), chapterImage.fileName());
        return imageService.decodeImage(chapterImage)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("image/webp"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                String.format("inline; filename=\"%s.webp\"", chapterImage.fileName()))
                        .body(bytes))
                .onErrorResume(IOException.class, e -> {
                    log.error("解码失败", e);
                    return Mono.just(ResponseEntity.status(500).body("解码失败".getBytes()));
                });
    }
    /**
     * 下载GIF图片（不加密，直接透传）
     */
    @GetMapping("/image/gif/{chapterId}/{fileName}")
    public Mono<ResponseEntity<byte[]>> gif(
            @PathVariable("chapterId") int chapterId,
            @PathVariable("fileName") String fileName) {
        log.debug("下载GIF图片: chapterId={}, fileName={}", chapterId, fileName);
        return imageService.downloadGif(chapterId, fileName)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("image/gif"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                String.format("inline; filename=\"%s.gif\"", fileName))
                        .body(bytes))
                .onErrorResume(IOException.class, e -> {
                    log.error("GIF下载失败", e);
                    return Mono.just(ResponseEntity.status(500).body("GIF下载失败".getBytes()));
                });
    }

    /**
     * 获取用户头像
     * @param filename 头像文件名（含后缀，如 abc123.jpg）
     */
    @GetMapping("/image/avatar/{filename:.+}")
    public Mono<ResponseEntity<byte[]>> avatar(@PathVariable("filename") String filename) {
        log.debug("获取用户头像: filename={}", filename);
        String contentType = resolveImageContentType(filename);
        return imageService.userAvatar(filename)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                String.format("inline; filename=\"%s\"", filename))
                        .body(bytes))
                .onErrorResume(Exception.class, e -> {
                    log.error("用户头像下载失败: filename={}", filename, e);
                    return Mono.just(ResponseEntity.status(500).body("头像下载失败".getBytes()));
                });
    }

    /** 根据文件后缀推断图片Content-Type */
    private String resolveImageContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    @GetMapping("/image/cover/{albumId}")
    public Mono<ResponseEntity<byte[]>> cover(@PathVariable("albumId") int albumId) {
        return imageService.albumCover(albumId)
                .map(bytes -> {
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("image/jpg"))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"cover.jpg\"")
                            .body(bytes);
                })
                .onErrorResume(IOException.class,e ->{
                    log.error("图片下载失败");
                    return Mono.just(ResponseEntity.status(500).body("封面下载失败".getBytes()));
                });
    }
}
