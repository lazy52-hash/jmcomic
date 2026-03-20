package com.lazy.jmcomic.api.v2.client;

import com.lazy.jmcomic.api.v2.annotation.SecureApi;
import com.lazy.jmcomic.api.v2.config.ApiDomainRegistry;
import com.lazy.jmcomic.api.v2.config.properties.SettingProperties;
import com.lazy.jmcomic.api.v2.constant.ApiPath;
import com.lazy.jmcomic.api.v2.pojo.ApiRequest;
import com.lazy.jmcomic.api.v2.task.ApiHttpingTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lazy.jmcomic.api.v2.constant.ApiRequest.HEADERS;

/**
 * @author lazy
 * @since 2026.3.15
 * <p>v2 API 客户端工厂，管理域名分配和故障轮询</p>
 * <p>顺序轮询存活域名列表，单站点无锁，通过 @SecureApi 切面透明注入请求签名及响应解密</p>
 */
@Slf4j
@Component
public class ApiClientFactory {

    @Autowired
    private WebClient webClient;

    @Autowired
    private ApiDomainRegistry apiDomainRegistry;

    @Autowired
    private ApiHttpingTask httpingTask;

    @Autowired
    private SettingProperties settingProperties;

    /**
     * 自注入代理引用，用于使占位方法调用 execute() 时能被 @SecureApi AOP 切面拦截
     * （Spring AOP 不拦截 this.method() 自调用，需通过代理触发）
     */
    @Lazy
    @Autowired
    private ApiClientFactory self;

    /** 持有 ApiDomainRegistry 内部引用，ping 后自动更新 */
    private CopyOnWriteArrayList<String> aliveDomains;

    /** 每个域名对应的客户端实例 */
    private final ConcurrentHashMap<String, ApiClient> clients = new ConcurrentHashMap<>();

    /** 轮询游标 */
    private final AtomicInteger cursor = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        this.aliveDomains = apiDomainRegistry.getAliveDomains();
    }

    /**
     * 顺序获取下一个存活域名，到达末尾重置游标
     */
    private synchronized String nextDomain() {
        int size = aliveDomains.size();
        if (size == 0) {
            log.warn("无可用API域名");
            return null;
        }
        int idx = cursor.get();
        if (idx >= size) {
            cursor.set(0);
            idx = 0;
        }
        String domain = aliveDomains.get(idx);
        cursor.set(idx + 1);
        log.debug("分配域名[{}]: {}", idx, domain);
        return domain;
    }

    private ApiClient getClient(String domain) {
        return clients.computeIfAbsent(domain, d -> new ApiClient(
                d, webClient,
                settingProperties.api().maxRetry(),
                settingProperties.api().timeout()
        ));
    }

    /**
     * 核心请求入口，被 @SecureApi 切面拦截：
     * 切面先往 request.headers() 注入 token/tokenParam，
     * 方法返回后切面在 Mono 流中对密文做 AES 解密。
     * @param request 请求封装对象
     * @return 解密后响应体 Mono
     */
    @SecureApi
    public Mono<String> execute(ApiRequest request) {
        String domain = nextDomain();
        if (domain == null) return Mono.empty();
        ApiClient client = getClient(domain);
        return client.doRequest(request)
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    log.warn("API域名 {} 请求失败，触发 httping 重检", domain);
                    httpingTask.ping(domain);
                }));
    }

    /** 获取 APP 设定 */
    public Mono<String> setting() {
        return self.execute(new ApiRequest(
                ApiPath.API_APP_SETTING, ApiRequest.Method.GET, new HashMap<>(HEADERS), null, null));
    }

    /** 搜索漫画
     * @param keyword 关键词
     * @param page    页码
     */
    public Mono<String> search(String keyword, int page) {
        Map<String, String> params = Map.of("search_query", keyword, "page", String.valueOf(page));
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_SEARCH, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }

    /** 热门话题 */
    public Mono<String> hotTags() {
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_HOT_TAGS, ApiRequest.Method.GET, new HashMap<>(HEADERS), null, null));
    }

    /** 随机推荐 */
    public Mono<String> randomRecommend() {
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_RANDOM_RECOMMEND, ApiRequest.Method.GET, new HashMap<>(HEADERS), null, null));
    }

    /** 漫画详情（占位）
     * @param albumId 漫画ID
     */
    public Mono<String> album(Integer albumId) {
        Map<String, String> params = Map.of("id", String.valueOf(albumId));
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_ALBUM, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }

    /** 章节信息（占位）
     * @param chapterId 章节ID
     */
    public Mono<String> chapter(Integer chapterId) {
        Map<String, String> params = Map.of("id", String.valueOf(chapterId));
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_CHAPTER, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> chapterComment(String mode,int id,int page){
        if(mode==null||mode.isBlank()){
            mode=page==1?"all":"1000";
        }
        Map<String, String> params = Map.of("mode", mode,"aid",String.valueOf(id),"page",String.valueOf(page));
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_COMMENT, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> read(int id){
        Map<String, String> params = Map.of("id", String.valueOf(id));
        return self.execute(new ApiRequest(
                ApiPath.API_COMIC_READ, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> downloadPage(int id){
        return self.execute(new ApiRequest(
                ApiPath.API_ALBUM_DOWNLOAD+"/"+id, ApiRequest.Method.GET, new HashMap<>(HEADERS), null, null));
    }
    public Mono<String> week(){
        return self.execute(new ApiRequest(
                ApiPath.API_WEEK, ApiRequest.Method.GET, new HashMap<>(HEADERS), null, null));

    }
    public Mono<String> novelList(String o,String t,Integer nid){
        Map<String, String> params = Map.of("o", o, "t", t, "nid", nid!=null?String.valueOf(nid):"");
        return self.execute(new ApiRequest(
                ApiPath.API_NOVEL_LIST, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> novelSearch(String keyword){
        Map<String, String> params = Map.of("search_query", keyword);
        return self.execute(new ApiRequest(
                ApiPath.API_NOVEL_SEARCH, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> novelDetail(int id){
        Map<String, String> params = Map.of("nid", String.valueOf(id));
        return self.execute(new ApiRequest(
                ApiPath.API_NOVEL_DETAIL, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> novelChapter(int ncid,String lang){
        Map<String, String> params = Map.of("ncid", String.valueOf(ncid),"lang",lang!=null&&!lang.isBlank()?lang:"tw");
        return self.execute(new ApiRequest(
                ApiPath.API_NOVEL_CHAPTERS, ApiRequest.Method.GET, new HashMap<>(HEADERS), params, null));
    }
    public Mono<String> login(String username,String password){
        return null;
    }
}
