package com.lazy.jmcomic.gateway.client;

import com.lazy.jmcomic.common.dto.ChapterImageDto;
import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
                .codecs(c -> c.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
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
                .bodyToMono(new ParameterizedTypeReference<AlbumPage<AlbumInfo>>() {})
                .doOnSubscribe(sub -> log.debug("开始搜索漫画: keyword={}", dto.getKeyword()))
                .doOnSuccess(result -> log.debug("搜索漫画成功: keyword={}, 结果数={}",
                        dto.getKeyword(), result != null ? result.totalElements() : 0))
                .doOnError(error -> log.error("搜索漫画失败: keyword={}, error={}",
                        dto.getKeyword(), error.getMessage()))
                .doFinally(signal -> log.debug("搜索漫画完成: keyword={}, signal={}",
                        dto.getKeyword(), signal));
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
                .bodyToMono(AlbumDetail.class)
                .doOnSubscribe(sub -> log.debug("开始获取漫画详情: albumId={}", albumId))
                .doOnSuccess(result -> log.debug("获取漫画详情成功: albumId={}", albumId))
                .doOnError(error -> log.error("获取漫画详情失败: albumId={}, error={}",
                        albumId, error.getMessage()))
                .doFinally(signal -> log.debug("获取漫画详情完成: albumId={}, signal={}",
                        albumId, signal));
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
                .bodyToMono(ChapterInfo.class)
                .doOnSubscribe(sub -> log.debug("开始获取章节信息: chapterId={}", chapterId))
                .doOnSuccess(result -> log.debug("获取章节信息成功: chapterId={}", chapterId))
                .doOnError(error -> log.error("获取章节信息失败: chapterId={}, error={}",
                        chapterId, error.getMessage()))
                .doFinally(signal -> log.debug("获取章节信息完成: chapterId={}, signal={}",
                        chapterId, signal));
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
                .bodyToMono(new ParameterizedTypeReference<List<Comment>>() {})
                .doOnSubscribe(sub -> log.debug("开始查询评论: videoId={}, page={}",
                        dto.videoId(), dto.page()))
                .doOnSuccess(result -> log.debug("查询评论成功: videoId={}, page={}, 结果数={}",
                        dto.videoId(), dto.page(), result != null ? result.size() : 0))
                .doOnError(error -> log.error("查询评论失败: videoId={}, page={}, error={}",
                        dto.videoId(), dto.page(), error.getMessage()))
                .doFinally(signal -> log.debug("查询评论完成: videoId={}, page={}, signal={}",
                        dto.videoId(), dto.page(), signal));
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
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.toEntity(byte[].class)
                                .doOnSuccess(entity -> {
                                    if (entity != null && entity.getBody() != null) {
                                        log.debug("图片解码成功: chapterId={}, fileName={}, size={} bytes",
                                                dto.chapterId(), dto.fileName(), entity.getBody().length);
                                    }
                                })
                                .doOnError(error -> log.error("图片解码转换失败: chapterId={}, fileName={}, error={}",
                                        dto.chapterId(), dto.fileName(), error.getMessage()))
                                .doFinally(signal -> log.debug("图片解码连接释放: chapterId={}, fileName={}, signal={}",
                                        dto.chapterId(), dto.fileName(), signal));
                    } else {
                        log.error("图片解码失败: chapterId={}, fileName={}, status={}",
                                dto.chapterId(), dto.fileName(), response.statusCode());
                        return response.releaseBody()
                                .then(Mono.<ResponseEntity<byte[]>>empty())
                                .doFinally(signal -> log.debug("图片解码失败后释放资源: chapterId={}, fileName={}, signal={}",
                                        dto.chapterId(), dto.fileName(), signal));
                    }
                })
                .doOnSubscribe(sub -> log.debug("开始解码图片: chapterId={}, fileName={}",
                        dto.chapterId(), dto.fileName()))
                .doOnError(error -> log.error("解码图片请求失败: chapterId={}, fileName={}, error={}",
                        dto.chapterId(), dto.fileName(), error.getMessage()))
                .doFinally(signal -> log.debug("解码图片完成: chapterId={}, fileName={}, signal={}",
                        dto.chapterId(), dto.fileName(), signal));
    }

    /** v2 搜索漫画 */
    public Mono<String> searchAlbumV2(String keyword, int page) {
        log.debug("v2搜索漫画: keyword={}, page={}", keyword, page);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/search?keyword={k}&page={p}", keyword, page)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始搜索漫画: keyword={}, page={}", keyword, page))
                .doOnSuccess(result -> log.debug("v2搜索漫画成功: keyword={}, page={}, 结果长度={}",
                        keyword, page, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2搜索漫画失败: keyword={}, page={}, error={}",
                        keyword, page, error.getMessage()))
                .doFinally(signal -> log.debug("v2搜索漫画完成: keyword={}, page={}, signal={}",
                        keyword, page, signal));
    }

    /** v2 漫画详情 */
    public Mono<String> getAlbumDetailV2(int albumId) {
        log.debug("v2漫画详情: albumId={}", albumId);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/{id}", albumId)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取漫画详情: albumId={}", albumId))
                .doOnSuccess(result -> log.debug("v2获取漫画详情成功: albumId={}, 结果长度={}",
                        albumId, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取漫画详情失败: albumId={}, error={}",
                        albumId, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取漫画详情完成: albumId={}, signal={}",
                        albumId, signal));
    }

    /** v2 热门话题 */
    public Mono<String> hotTagsV2() {
        log.debug("v2热门话题");
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/hot-tags")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取热门话题"))
                .doOnSuccess(result -> log.debug("v2获取热门话题成功, 结果长度={}",
                        result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取热门话题失败: error={}", error.getMessage()))
                .doFinally(signal -> log.debug("v2获取热门话题完成: signal={}", signal));
    }

    /** v2 随机推荐 */
    public Mono<String> randomRecommendV2() {
        log.debug("v2随机推荐");
        return webClient.get()
                .uri(API_PREFIX_V2 + "/album/random-recommend")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取随机推荐"))
                .doOnSuccess(result -> log.debug("v2获取随机推荐成功, 结果长度={}",
                        result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取随机推荐失败: error={}", error.getMessage()))
                .doFinally(signal -> log.debug("v2获取随机推荐完成: signal={}", signal));
    }

    /** v2 章节信息 */
    public Mono<String> getChapterInfoV2(int chapterId) {
        log.debug("v2章节信息: chapterId={}", chapterId);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/chapter/{id}", chapterId)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取章节信息: chapterId={}", chapterId))
                .doOnSuccess(result -> log.debug("v2获取章节信息成功: chapterId={}, 结果长度={}",
                        chapterId, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取章节信息失败: chapterId={}, error={}",
                        chapterId, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取章节信息完成: chapterId={}, signal={}",
                        chapterId, signal));
    }

    /** v2 章节评论 */
    public Mono<String> chapterCommentV2(int id, int page, String mode) {
        log.debug("v2章节评论: id={}, page={}, mode={}", id, page, mode);
        return webClient.get()
                .uri(u -> u.path(API_PREFIX_V2 + "/chapter/{id}/comment/{page}")
                        .queryParam("mode", mode)
                        .build(id, page))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取章节评论: id={}, page={}, mode={}", id, page, mode))
                .doOnSuccess(result -> log.debug("v2获取章节评论成功: id={}, page={}, 结果长度={}",
                        id, page, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取章节评论失败: id={}, page={}, mode={}, error={}",
                        id, page, mode, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取章节评论完成: id={}, page={}, signal={}",
                        id, page, signal));
    }

    /** v2 章节阅读图片列表 */
    public Mono<String> readV2(int id) {
        log.debug("v2章节阅读: id={}", id);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/chapter/read/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取章节阅读: id={}", id))
                .doOnSuccess(result -> log.debug("v2获取章节阅读成功: id={}, 结果长度={}",
                        id, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取章节阅读失败: id={}, error={}",
                        id, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取章节阅读完成: id={}, signal={}",
                        id, signal));
    }

    /** v2 小说搜索 */
    public Mono<String> novelSearchV2(String keyword) {
        log.debug("v2小说搜索: keyword={}", keyword);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/search?search_query={k}", keyword)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始搜索小说: keyword={}", keyword))
                .doOnSuccess(result -> log.debug("v2搜索小说成功: keyword={}, 结果长度={}",
                        keyword, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2搜索小说失败: keyword={}, error={}",
                        keyword, error.getMessage()))
                .doFinally(signal -> log.debug("v2搜索小说完成: keyword={}, signal={}",
                        keyword, signal));
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
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取小说列表: o={}, t={}, nid={}", o, t, nid))
                .doOnSuccess(result -> log.debug("v2获取小说列表成功: o={}, t={}, 结果长度={}",
                        o, t, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取小说列表失败: o={}, t={}, nid={}, error={}",
                        o, t, nid, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取小说列表完成: o={}, t={}, signal={}",
                        o, t, signal));
    }

    /** v2 小说详情 */
    public Mono<String> novelDetailV2(int nid) {
        log.debug("v2小说详情: nid={}", nid);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/{nid}", nid)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取小说详情: nid={}", nid))
                .doOnSuccess(result -> log.debug("v2获取小说详情成功: nid={}, 结果长度={}",
                        nid, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取小说详情失败: nid={}, error={}",
                        nid, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取小说详情完成: nid={}, signal={}",
                        nid, signal));
    }

    /** v2 小说章节内容 */
    public Mono<String> novelChapterV2(int ncid, String lang) {
        log.debug("v2小说章节: ncid={}, lang={}", ncid, lang);
        return webClient.get()
                .uri(API_PREFIX_V2 + "/novel/chapter/{cid}?lang={lang}", ncid, lang)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(sub -> log.debug("v2开始获取小说章节: ncid={}, lang={}", ncid, lang))
                .doOnSuccess(result -> log.debug("v2获取小说章节成功: ncid={}, lang={}, 结果长度={}",
                        ncid, lang, result != null ? result.length() : 0))
                .doOnError(error -> log.error("v2获取小说章节失败: ncid={}, lang={}, error={}",
                        ncid, lang, error.getMessage()))
                .doFinally(signal -> log.debug("v2获取小说章节完成: ncid={}, lang={}, signal={}",
                        ncid, lang, signal));
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
                .toEntity(byte[].class)
                .doOnSubscribe(sub -> log.debug("开始下载GIF: chapterId={}, fileName={}", chapterId, fileName))
                .doOnSuccess(entity -> {
                    if (entity != null && entity.getBody() != null) {
                        log.debug("下载GIF成功: chapterId={}, fileName={}, size={} bytes",
                                chapterId, fileName, entity.getBody().length);
                    } else {
                        log.debug("下载GIF成功但响应体为空: chapterId={}, fileName={}", chapterId, fileName);
                    }
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("下载GIF失败: chapterId={}, fileName={}, status={}, body={}",
                                chapterId, fileName, ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("下载GIF失败: chapterId={}, fileName={}, error={}",
                                chapterId, fileName, error.getMessage());
                    }
                })
                .doFinally(signal -> log.debug("下载GIF连接释放: chapterId={}, fileName={}, signal={}",
                        chapterId, fileName, signal));
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
                .toEntity(byte[].class)
                .doOnSubscribe(sub -> log.debug("开始获取用户头像: filename={}", filename))
                .doOnSuccess(entity -> {
                    if (entity != null && entity.getBody() != null) {
                        log.debug("获取用户头像成功: filename={}, size={} bytes",
                                filename, entity.getBody().length);
                    } else {
                        log.debug("获取用户头像成功但响应体为空: filename={}", filename);
                    }
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("获取用户头像失败: filename={}, status={}, body={}",
                                filename, ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("获取用户头像失败: filename={}, error={}",
                                filename, error.getMessage());
                    }
                })
                .doFinally(signal -> log.debug("获取用户头像连接释放: filename={}, signal={}",
                        filename, signal));
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
                .toEntity(byte[].class)
                .doOnSubscribe(sub -> log.debug("开始获取漫画封面: filename={}", filename))
                .doOnSuccess(entity -> {
                    if (entity != null && entity.getBody() != null) {
                        log.debug("获取漫画封面成功: filename={}, size={} bytes",
                                filename, entity.getBody().length);
                    } else {
                        log.debug("获取漫画封面成功但响应体为空: filename={}", filename);
                    }
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("获取漫画封面失败: filename={}, status={}, body={}",
                                filename, ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("获取漫画封面失败: filename={}, error={}",
                                filename, error.getMessage());
                    }
                })
                .doFinally(signal -> log.debug("获取漫画封面连接释放: filename={}, signal={}",
                        filename, signal));
    }
}