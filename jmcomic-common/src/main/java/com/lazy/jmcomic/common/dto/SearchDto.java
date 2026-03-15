package com.lazy.jmcomic.common.dto;

import com.lazy.jmcomic.common.enums.AlbumTag;
import com.lazy.jmcomic.common.enums.AlbumTime;
import com.lazy.jmcomic.common.enums.AlbumType;
import com.lazy.jmcomic.common.enums.SortType;

/**
 * web 版本搜索DTO
 */
public class SearchDto{

    /** 搜索keyword */
    private String keyword;
    /** 搜索类型 */
    private AlbumType type;
    /** 漫画更新时间 */
    private AlbumTime time;
    /** 排序方式 */
    private SortType sort;
    /** 标签 */
    private AlbumTag tag;
    private int pageNo;

    public SearchDto(String keyword) {
        this.keyword = keyword;
        this.tag= AlbumTag.DEFAULT;
        this.sort=SortType.DEFAULT;
        this.type= AlbumType.DEFAULT;
        this.time= AlbumTime.DEFAULT;
        this.pageNo=1;

    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public AlbumType getType() {
        return type;
    }

    public void setType(AlbumType type) {
        this.type = type;
    }

    public AlbumTime getTime() {
        return time;
    }

    public void setTime(AlbumTime time) {
        this.time = time;
    }

    public SortType getSort() {
        return sort;
    }

    public void setSort(SortType sort) {
        this.sort = sort;
    }

    public AlbumTag getTag() {
        return tag;
    }

    public void setTag(AlbumTag tag) {
        this.tag = tag;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
}