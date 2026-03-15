package com.lazy.jmcomic.api.v1.service.impl;

import com.lazy.jmcomic.api.v1.client.WebCrawlFactory;
import com.lazy.jmcomic.api.v1.constant.selector.AlbumDetailPage;
import com.lazy.jmcomic.api.v1.constant.selector.SearchPage;
import com.lazy.jmcomic.api.v1.service.IAlbumService;
import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.pojo.AlbumDetail;
import com.lazy.jmcomic.common.pojo.AlbumInfo;
import com.lazy.jmcomic.common.pojo.AlbumPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lazy
 * @since 2026.3.10
 * <p></p>
 */
@Slf4j
@Service
public class AlbumServiceImpl implements IAlbumService {
    @Autowired
    private WebCrawlFactory factory;
    @Override
    public AlbumPage<AlbumInfo> findAlbum(SearchDto dto) throws IOException {
        //Document document= Jsoup.parse(new File(""));
        log.debug("开始搜索漫画");
        Document document=factory.execute((cli)->cli.searchPage(dto)).parse();
        String message=document.select(SearchPage.COMIC_SEARCH_MESSAGE_SELECTION).first().text();
        Elements elements=document.select(SearchPage.COMIC_ELEMENTS_SELECTION);
        if(!elements.isEmpty()){
            List<AlbumInfo> wrappers=new ArrayList<AlbumInfo>();
            elements.forEach(item->{
                wrappers.add(extractAlbumInfo(item));
            });
            Elements totalElements=document.select(SearchPage.COMIC_TOTAL_ELEMENTS_SELECTION);
            Elements totalPages=document.select(SearchPage.COMIC_TOTAL_PAGES_SELECTION).prev();
            return new AlbumPage<AlbumInfo>(wrappers,
                    dto.getPageNo(),
                    totalElements.isEmpty()?0:parseInt(totalElements.first().text()),
                    totalPages.isEmpty()?1:parseInt(totalPages.first().text()),message);
        }
        log.debug("漫画搜索成功");
        return new AlbumPage<>(message);
    }

    @Override
    public AlbumDetail findAlbumById(int id) throws IOException {
        log.debug("开始解析漫画详情：{}",id);
        Document document=factory.execute(cli->cli.albumPage(id)).parse();
        //基础数据
        String name = document.select(AlbumDetailPage.NAME_SELECTION).text();
        String cover = document.select(AlbumDetailPage.COVER_SELECTION).attr("src");
        //详情容器
        Element container=document.select(AlbumDetailPage.DETAIL_CONTAINER_SELECTION).first();
        if(container==null){
            log.warn("漫画详情选择失败,切换备选方案");
            container=document.select(".panel-body>.row>div:last-child").first();
        }
        if(container==null){
            log.error("漫画详情容器解析失败, id={}", id);
            return null;
        }
        String[] actors = convertToArray(container.select(AlbumDetailPage.ACTORS_SELECTION));
        String[] authors = convertToArray(container.select(AlbumDetailPage.AUTHORS_SELECTION));
        String[] tags = convertToArray(container.select(AlbumDetailPage.TAGS_SELECTION));
        String description = null;
        int photoCount = 0;
        String uploadUser = null;
        String createdTime = null;
        String lastUpdateTime = null;
        String views = null;
        String likes = null;
        Elements detailElements=container.select(AlbumDetailPage.DETAIL_SELECTIONS);
        if(!detailElements.isEmpty()){
            try{
                //叙述
                description = detailElements.get(1).text();
                //页数
                photoCount = extractInt(detailElements.get(2).text());
                //上传用户
                uploadUser = detailElements.get(3).text();
                //上传/更新时间 查看/点赞量父容器
                Element element=detailElements.get(4);
                Elements timeElements=element.select(AlbumDetailPage.TIME_SELECTION);
                createdTime = timeElements.first().attr("content");
                lastUpdateTime = timeElements.last().attr("content");
                views = element.children().get(2).children().get(0).text();
                likes = element.children().get(3).select(AlbumDetailPage.LIKES_SELECTION).text();
            }catch (Exception e){
                log.warn("解析漫画详情字段失败, id={}: {}", id, e.getMessage());
            }
        }
        //评论数
        int commitCount = 0;
        Elements commentElements=document.select(AlbumDetailPage.COMMITS_SELECTION);
        if(!commentElements.isEmpty()){
            commitCount = parseInt(commentElements.first().text());
        }
        //章节列表
        AlbumDetail.ChapterInfo[] chapters = null;
        Elements chapterElements=document.select(AlbumDetailPage.CHAPTER_SELECTION);
        if(!chapterElements.isEmpty()){
            chapterElements=chapterElements.first().select("a[data-album]");
            chapters = new AlbumDetail.ChapterInfo[chapterElements.size()];
            for(int i=0;i<chapterElements.size();i++){
                chapters[i]=new AlbumDetail.ChapterInfo(parseInt(chapterElements.get(i).attr("data-album")),chapterElements.get(i).select("h3.h2_series").text());
            }
        }
        //封面转网关路径
        cover = "/image/cover/" + id + ".jpg";
        String[] works = convertToArray(document.select(AlbumDetailPage.WORKS_SELECTION));
        log.debug("解析漫画详情:{}成功", id);
        return new AlbumDetail(id, name, actors, authors, photoCount, description,
                uploadUser, createdTime, lastUpdateTime, chapters, commitCount,
                cover, tags, likes, views, works);
    }
    /** 从搜索结果元素中提取漫画信息 */
    private AlbumInfo extractAlbumInfo(Element item){
        try{
            int id = 0;
            String comicHref = item.select(SearchPage.COMIC_HREF_SELECTION).first().attr("href");
            Matcher matcher = SearchPage.URL_COMIC_ID.matcher(comicHref);
            if(matcher.find()){
                id = parseInt(matcher.group(1));
            }
            String name = item.select(SearchPage.COMIC_NAME_SELECTION).first().text();
            String[] authors = convertToArray(item.select(SearchPage.COMIC_AUTHORS_SELECTION));
            String likes = item.select(SearchPage.COMIC_LIKES_SELECTION).first().text();
            //部分可能不存在的值，单独捕捉避免影响整体解析
            String tag = safeText(item, SearchPage.COMIC_TAG_SELECTION);
            String[] tags = safeConvertToArray(item, SearchPage.COMIC_TAGS_SELECTION);
            String type = safeText(item, SearchPage.COMIC_TYPE_SELECTION);
            String cover = safeAttr(item, SearchPage.COMIC_COVER_SELECTION, "data-original");
            boolean isUpdated = !item.select(SearchPage.COMIC_IS_UPDATED_SELECTION).isEmpty();
            return new AlbumInfo(id, name, authors, likes, tag, tags, type, cover, isUpdated);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }
    /** 安全提取元素文本，不存在时返回null */
    private String safeText(Element parent, String selector){
        try{
            Element el = parent.select(selector).first();
            return el != null ? el.text() : null;
        }catch (Exception e){
            log.warn("解析字段失败, selector={}: {}", selector, e.getMessage());
            return null;
        }
    }
    /** 安全提取元素属性，不存在时返回null */
    private String safeAttr(Element parent, String selector, String attr){
        try{
            Element el = parent.select(selector).first();
            return el != null ? el.attr(attr) : null;
        }catch (Exception e){
            log.warn("解析字段失败, selector={}: {}", selector, e.getMessage());
            return null;
        }
    }
    /** 安全提取元素数组，不存在时返回空数组 */
    private String[] safeConvertToArray(Element parent, String selector){
        try{
            return convertToArray(parent.select(selector));
        }catch (Exception e){
            log.warn("解析字段失败, selector={}: {}", selector, e.getMessage());
            return new String[0];
        }
    }
    private Integer parseInt(String s){
        try{
            return Integer.parseInt(s);
        }catch (Exception e){

            return 0;
        }
    }
    private String[] convertToArray(Elements elements){
        return elements.textNodes().stream()
                .map(TextNode::text)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .toArray(String[]::new);
    }
    private Integer extractInt(String s){
        if (s == null || s.trim().isEmpty()) {
            return 0;
        }
        Pattern pattern=Pattern.compile("-?\\d+");
        Matcher matcher=pattern.matcher(s);
        if(matcher.find()){
            return Integer.parseInt(matcher.group());
        }else{
            return 0;
        }
    }
}
