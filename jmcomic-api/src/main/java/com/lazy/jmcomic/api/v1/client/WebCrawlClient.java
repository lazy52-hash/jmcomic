package com.lazy.jmcomic.api.v1.client;

import com.lazy.jmcomic.api.v1.config.properties.WebsiteProperties;
import com.lazy.jmcomic.api.v1.constant.WebApi;
import com.lazy.jmcomic.api.v1.constant.WebPath;
import com.lazy.jmcomic.api.v1.constant.Website;
import com.lazy.jmcomic.api.v1.task.WebsiteHttpingTask;
import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.enums.AlbumTime;
import com.lazy.jmcomic.common.enums.AlbumType;
import com.lazy.jmcomic.common.enums.SortType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
/**
 * @author lazy
 * @since 2026.3.8
 * Web站点爬取请求客户端（非Spring Bean，由WebCrawlFactory手动创建并注入依赖）
 */
@Slf4j
public final class WebCrawlClient{
    private final WebsiteHttpingTask task;
    private final WebsiteProperties properties;
    private final Proxy proxy;
    private final ConcurrentHashMap<String, String> cookies;
    private final String website;
    private final String baseurl;
    private final ReentrantLock lock=new ReentrantLock();
    private volatile long lastReset=System.currentTimeMillis();
    private volatile int requests;
    /**
     * 创建爬取客户端实例
     * @param website 站点域名
     * @param properties 网站配置属性
     * @param proxy HTTP代理
     * @param cookies 共享Cookie容器
     * @param task 站点连通性检测任务
     */
    WebCrawlClient(String website, WebsiteProperties properties, Proxy proxy,
                   ConcurrentHashMap<String, String> cookies, WebsiteHttpingTask task){
        this.website = website;
        this.baseurl= Website.PROT+website;
        this.properties = properties;
        this.proxy = proxy;
        this.cookies = cookies;
        this.task = task;
    }

    private static final int RESET_LOCK_INTERVAL=60*1000;
    private boolean canRequest(){
        try{
            lock.lock();
            if(System.currentTimeMillis()-lastReset<=RESET_LOCK_INTERVAL){
                return requests<properties.rateLimit().minute();
            }
            lastReset=System.currentTimeMillis();
            requests=0;
            return true;
        }finally{
            lock.unlock();
        }
    }
    /**
     * 搜索漫画接口
     * @param search 搜索对象
     * @return Jsoup格式化Response对象
     */
    public Connection.Response searchPage(SearchDto search){
        log.debug("请求搜索keyword:{},类型:{},标签:{},时间:{},排序方式:{}",
                search.getKeyword(),
                search.getType().getDescription(),
                search.getTag().getDescription(),
                search.getTime().getDescription(),
                search.getSort().getDescription()
        );
        return connect(generateUrl(WebPath.COMIC_SEARCH+"/"+search.getTag().getValue(),
                "search_query",search.getKeyword(),
                SortType.QUERY_KEY,search.getSort().getValue(),
                AlbumTime.QUERY_KEY,search.getTime().getValue(),
                AlbumType.QUERY_KEY,String.valueOf(search.getType().getValue()),
                "page",String.valueOf(search.getPageNo())
        ));
    }

    /**
     * 根据漫画ID获取漫画
     * @param comicId 漫画ID
     * @return Jsoup格式化Response对象
     */
    public Connection.Response albumPage(Integer comicId){
        log.debug("请求漫画:{}",comicId);
        return connect(String.format(WebPath.COMIC_ALBUM,comicId));
    }
    /**
     * 只根据章节ID搜索单章节
     * @param chapterId 章节ID
     * @return Jsoup格式化Response对象
     */
    public Connection.Response chapterPage(Integer chapterId){
        log.debug("请求漫画章节:{}",chapterId);
        return connect(String.format(WebPath.COMIC_CHAPTER,chapterId));
    }

    /**
     * 请求章节评论（POST表单提交）
     * @param dto 评论查询参数（videoId, series, page）
     * @return Jsoup格式化Response对象
     */
    public Connection.Response commentPage(CommentQueryDataDto dto) {
        log.debug("请求评论: videoId={}, series={}, page={}", dto.videoId(), dto.series(), dto.page());
        WebsiteProperties.Connection connection = properties.connection();
        String requestPath = baseurl + WebApi.COMMENT;
        try {
            lock.lock();
            if (canRequest()) {
                requests++;
                return Jsoup.connect(requestPath)
                        .method(Connection.Method.POST)
                        .timeout(connection.timeout())
                        .header("Host", website)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .referrer(baseurl)
                        .headers(Website.HEADERS)
                        .cookies(cookies)
                        .proxy(proxy)
                        .ignoreContentType(true)
                        .data("video_id", String.valueOf(dto.videoId()))
                        //暂时不要了,这个参数在详情页有但是我不知道有什么用，暂时不搞详情页了
//                        .data("series", dto.series()==null?"":String.valueOf(dto.series()))
                        .data("page", String.valueOf(dto.page()))
                        .execute().charset("UTF-8");
            } else {
                log.info("已达客户端请求上限,当前已计时:{}s", ((double) System.currentTimeMillis() - lastReset) / 1000);
                return null;
            }
        } catch (SocketTimeoutException e) {
            log.warn("评论请求超时: {}", requestPath);
            task.ping(website);
        } catch (ConnectException e) {
            log.warn("评论请求连接被拒: {} -> {}", requestPath, e.getMessage());
            task.ping(website);
        } catch (IOException e) {
            log.info("评论请求失败: {}, message: {}", requestPath, e.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
    }

    public Connection.Response loginApi(String username, String password) throws IOException {
        String requestPath = baseurl + WebApi.LOGIN;
        log.debug("请求登录:{}", requestPath);
        try {
            lock.lock();
            if (canRequest()) {
                WebsiteProperties.Connection connection=properties.connection();
                return Jsoup.connect(baseurl+ WebApi.LOGIN)
                        .method(Connection.Method.POST)
                        .timeout(connection.timeout())
                        .header("Host",website)
                        .referrer(baseurl)
                        .headers(Website.HEADERS)
                        .cookies(cookies)
                        .proxy(proxy)
                        .ignoreContentType(true)
                        .data("username", username)
                        .data("password", password)
                        .data("login_remember", "on")
                        .data("submit_login", "1")
                        .execute().charset("UTF-8");
            }
        } catch (SocketTimeoutException e) {
            log.warn("登录请求超时: {}", requestPath);
            task.ping(website);
        } catch (ConnectException e) {
            log.warn("登录请求连接被拒: {} -> {}", requestPath, e.getMessage());
            task.ping(website);
        } catch (IOException e) {
            log.info("登录请求失败: {}, message: {}", requestPath, e.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
                
    }
    private Connection.Response connect(String path){
        WebsiteProperties.Connection connection=properties.connection();
        String requestPath=baseurl+path;
        try{
            lock.lock();
            if(canRequest()){
                log.debug("发起请求:{}",requestPath);
                requests++;
                return Jsoup.connect(requestPath)
                        .timeout(connection.timeout())
                        .header("Host",website)
                        .referrer(baseurl)
                        .headers(Website.HEADERS)
                        .cookies(cookies)
                        .proxy(proxy)
                        .ignoreContentType(true)
                        .execute().charset("UTF-8");
            }else{
                log.info("已达客户端请求上限,当前已计时:{}s",((double)System.currentTimeMillis()-lastReset)/1000);
                return null;
            }
        }catch (SocketTimeoutException e){//超时和连接被拒绝触发连通性检查
            log.warn("站点连接超时:{}",requestPath);
            task.ping(website);
        }catch (ConnectException e) {
            log.warn("站点连接被拒: {} -> {}", requestPath, e.getMessage());
            task.ping(website);
        }catch (IOException e) {
            log.info("搜索失败:{},message:{}",requestPath,e.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
    }
    private String generateUrl(String path, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append("?");
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length && args[i + 1] != null && !args[i + 1].isEmpty()) {
                sb.append(args[i]).append("=").append(args[i + 1]).append("&");
            }
        }
        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        } else if (result.endsWith("?")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
