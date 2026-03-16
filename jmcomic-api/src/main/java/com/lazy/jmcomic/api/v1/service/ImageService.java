package com.lazy.jmcomic.api.v1.service;

import com.lazy.jmcomic.common.dto.ChapterImageDto;
import reactor.core.publisher.Mono;

public interface ImageService {
    Mono<byte[]> albumCover(String filename);
    Mono<byte[]> decodeImage(ChapterImageDto image);
    /** 下载GIF图片（不加密，直接透传） */
    Mono<byte[]> downloadGif(int chapterId, String fileName);
    /** 下载用户头像 */
    Mono<byte[]> userAvatar(String filename);
}
