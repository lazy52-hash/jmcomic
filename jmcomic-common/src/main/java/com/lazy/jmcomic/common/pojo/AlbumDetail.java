package com.lazy.jmcomic.common.pojo;

/**
 * 漫画信息对象
 */
public record AlbumDetail(
        /** 漫画id */
        int albumId,
        /** 名字 */
        String name,
        /** 人物列表 */
        String[] actors,
        /** 作者列表 */
        String[] authors,
        /** 图片数量 */
        int photoCount,
        String description,
        /** 上传者 */
        String uploadUser,
        /** 漫画创建时间 */
        String createdTime,
        /** 最近更新时间 */
        String lastUpdateTime,
        /** 章节列表 */
        ChapterInfo[] chapters,
        /** 评论数 */
        int commitCount,
        /** 封面数据 */
        String cover,
        /** 标签数据 */
        String[] tags,
        /** 爱心 */
        String likes,
        /** 查看量 */
        String views,
        /** 我也不知道是什么数据 */
        String[] works) {

    public record ChapterInfo(Integer id, String name) {}
}