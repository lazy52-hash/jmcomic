package com.lazy.jmcomic.gateway.controller;

import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.enums.AlbumTag;
import com.lazy.jmcomic.common.enums.AlbumTime;
import com.lazy.jmcomic.common.enums.AlbumType;
import com.lazy.jmcomic.common.enums.SortType;
import com.lazy.jmcomic.common.pojo.AlbumDetail;
import com.lazy.jmcomic.common.pojo.AlbumInfo;
import com.lazy.jmcomic.common.pojo.AlbumPage;
import com.lazy.jmcomic.gateway.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>漫画网关控制器</p>
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/album")
public class AlbumController {
    @Autowired
    private ApiClient apiClient;

    /**
     * 搜索漫画
     * @param keyword 搜索关键词
     * @param tag 标签（""=全部, another, doujin, hanman, meiman, short, singe）
     * @param sort 排序（mv=点阅, mp=图片, tf=爱心, mr=最新）
     * @param time 时间（""=全部, t=今日, w=本周, m=本月）
     * @param type 类型（0=全部, 1=作者, 2=作品, 3=标签, 4=人物）
     * @param page 页码
     * @return 分页漫画列表
     */
    @GetMapping("/search")
    public Mono<AlbumPage<AlbumInfo>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page) {
        log.debug("网关搜索漫画: keyword={}, tag={}, sort={}, time={}, type={}, page={}",
                keyword, tag, sort, time, type, page);
        SearchDto dto = new SearchDto(keyword);
        if (tag != null) dto.setTag(AlbumTag.getByValue(tag));
        if (sort != null) dto.setSort(SortType.getByValue(sort));
        if (time != null) dto.setTime(AlbumTime.getByValue(time));
        if (type != null) dto.setType(AlbumType.getByValue(type));
        dto.setPageNo(page);
        return apiClient.searchAlbum(dto);
    }

    /**
     * 获取漫画详情
     * @param id 漫画ID
     * @return 漫画详情
     */
    @GetMapping("/{id}")
    public Mono<AlbumDetail> detail(@PathVariable int id) {
        log.debug("网关获取漫画详情: id={}", id);
        return apiClient.getAlbumDetail(id);
    }
}