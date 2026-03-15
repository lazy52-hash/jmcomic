package com.lazy.jmcomic.common.pojo;

/**
 * 章节信息
 */
public record ChapterInfo(
        Integer albumId,
        /** 图片数 */
        int photoCount,
        /** 评论数 */
        int commitCount,
        /** scrambleId拼图还原用的，非常重要 */
        int scrambleId,
        /** 图片名称虽然是自增的但是中间有空缺情况 */
        String[] photoNames) {

    public boolean isEmpty() {
        return albumId == null || photoCount == 0 || scrambleId == 0 || photoNames == null || photoNames.length == 0;
    }
}