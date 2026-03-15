package com.lazy.jmcomic.common.pojo;

/**
 * 在搜索页面的漫画数据
 */
public record AlbumInfo(
        int id,
        String name,
        String[] authors,
        /** 爱心数量(模糊) */
        String likes,
        /** 首个Tag(也可能是系统自动鉴定的?) */
        String tag,
        String[] tags,
        String type,
        /** 封面数据url */
        String cover,
        /** 页面中的[更新]元素 */
        boolean isUpdated) {
}