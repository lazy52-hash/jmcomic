package com.lazy.jmcomic.gateway.client;

import com.lazy.jmcomic.common.dto.ChapterImageDto;
import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>jmcomic-api 服务调用客户端，通过负载均衡调用后端API</p>
 */
@Slf4j
@Service
public class ApiClient {
    private static final String API_PREFIX = "/jmapi/v1";
    private static final String API_PREFIX_V2 = "/jmapi/v2";
    private final WebClient webClient;
    public ApiClient(WebClient.Builder builder) {
        this.webClient = builder
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .baseUrl("lb://jmcomic-api")
                .build();
    }

    /**
     * 搜索漫画
     * @param dto 搜索参数
     * @return 分页漫画列表
     */
    public Mono<AlbumPage<AlbumInfo>> searchAlbum(SearchDto dto) {
        log.debug("调用API搜索漫画: keyword={}", dto.getKeyword());
        return webClient.post()
                .uri(API_PREFIX + "/album/search")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    /**
     * 获取漫画详情
     * @param albumId 漫画ID
     * @return 漫画详情
     */
    public Mono<AlbumDetail> getAlbumDetail(int albumId) {
        log.debug("调用API获取漫画详情: albumId={}", albumId);
        return webClient.get()
                .uri(API_PREFIX + "/album/{id}", albumId)
                .retrieve()
                .bodyToMono(AlbumDetail.class);
    }

    /**
     * 获取章节信息
     * @param chapterId 章节ID
     * @return 章节信息
     */
    public Mono<ChapterInfo> getChapterInfo(int chapterId) {
        log.debug("调用API获取章节信息: chapterId={}", chapterId);
        return webClient.get()
                .uri(API_PREFIX + "/chapter/{id}", chapterId)
                .retrieve()
                .bodyToMono(ChapterInfo.class);
    }

    /**
     * 查询章节评论
     * @param dto 评论查询参数
     * @return 评论列表
     */
    public Mono<List<Comment>> getComments(CommentQueryDataDto dto) {
        log.debug("调用API查询评论: videoId={}, page={}", dto.videoId(), dto.page());
        return webClient.post()
                .uri(API_PREFIX + "/chapter/comment")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    /**
     * 解码章节图片
     * @param dto 章节图片参数
     * @return 原始图片字节（含响应头）
     */
    public Mono<ResponseEntity<byte[]>> decodeImage(ChapterImageDto dto) {
        log.debug("调用API解码图片: chapterId={}, fileName={}", dto.chapterId(), dto.fileName());
        return webClient.post()
                .uri(API_PREFIX + "/image/decode")
                .bodyValue(dto)
                .retrieve()
                .toEntity(byte[].class);
    }

    // ==================== v2 接口（返回原始解密 JSON 字符串） ====================

    /** v2 搜索漫画 */
    public Mono<String> searchAlbumV2(String keyword, int page) {
        log.debug("v2搜索漫画: keyword={}, page={}", keyword, page);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/search?keyword={k}&page={p}", keyword, page)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 漫画详情 */
    public Mono<String> getAlbumDetailV2(int albumId) {
        log.debug("v2漫画详情: albumId={}", albumId);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/{id}", albumId)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 热门话题 */
    public Mono<String> hotTagsV2() {
        log.debug("v2热门话题");
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/hot-tags")
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 随机推荐 */
    public Mono<String> randomRecommendV2() {
        log.debug("v2随机推荐");
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/random-recommend")
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 章节信息 */
    public Mono<String> getChapterInfoV2(int chapterId) {
        log.debug("v2章节信息: chapterId={}", chapterId);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/chapter/{id}", chapterId)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 章节评论 */
    public Mono<String> chapterCommentV2(int id, int page, String mode) {
        log.debug("v2章节评论: id={}, page={}, mode={}", id, page, mode);
        return webClient.get()
                .uri(u -> u.path(API_PREFIX_V2 + "/chapter/{id}/comment/{page}")
                        .queryParam("mode", mode)
                        .build(id, page))
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 章节阅读图片列表 */
    public Mono<String> readV2(int id) {
        log.debug("v2章节阅读: id={}", id);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/chapter/read/{id}", id)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 小说搜索 */
    public Mono<String> novelSearchV2(String keyword) {
        log.debug("v2小说搜索: keyword={}", keyword);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/search?search_query={k}", keyword)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 小说列表 */
    public Mono<String> novelListV2(String o, String t, Integer nid) {
        log.debug("v2小说列表: o={}, t={}, nid={}", o, t, nid);
        return webClient.get()
                .uri(u -> u.path(API_PREFIX_V2 + "/novel/list")
                        .queryParam("o", o)
                        .queryParam("t", t)
                        .queryParam("id", nid != null ? nid : "")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 小说详情 */
    public Mono<String> novelDetailV2(int nid) {
        log.debug("v2小说详情: nid={}", nid);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/{nid}", nid)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** v2 小说章节内容 */
    public Mono<String> novelChapterV2(int ncid, String lang) {
        log.debug("v2小说章节: ncid={}, lang={}", ncid, lang);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/chapter/{cid}?lang={lang}", ncid, lang)
                .retrieve()
                .bodyToMono(String.class);
    }

    // ==================== v1 图片接口 ====================

    /**
     * 下载GIF图片
     * @param chapterId 章节ID
     * @param fileName 文件名（无后缀）
     * @return GIF图片字节（含响应头）
     */
    public Mono<ResponseEntity<byte[]>> getGifImage(int chapterId, String fileName) {
        log.debug("调用API下载GIF: chapterId={}, fileName={}", chapterId, fileName);
        return webClient.get()
                .uri(API_PREFIX + "/image/gif/{chapterId}/{fileName}", chapterId, fileName)
                .retrieve()
                .toEntity(byte[].class);
    }

    /**
     * 获取用户头像
     * @param filename 头像文件名（含后缀）
     * @return 头像图片字节（含响应头）
     */
    public Mono<ResponseEntity<byte[]>> getUserAvatar(String filename) {
        log.debug("调用API获取用户头像: filename={}", filename);
        return webClient.get()
                .uri(API_PREFIX + "/image/avatar/{filename}", filename)
                .retrieve()
                .toEntity(byte[].class);
    }

    /**
     * 获取漫画封面
     * @param filename 漫画ID
     * @return 封面图片字节（含响应头）
     */
    public Mono<ResponseEntity<byte[]>> getAlbumCover(String filename) {
        log.debug("调用API获取封面:{}", filename);
        return webClient.get()
                .uri(API_PREFIX + "/image/cover/{filename}", filename)
                .retrieve()
                .toEntity(byte[].class);
    }

}