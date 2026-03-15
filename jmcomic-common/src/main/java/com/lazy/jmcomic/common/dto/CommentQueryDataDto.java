package com.lazy.jmcomic.common.dto;

/**
 * 章节评论查询参数
 * @param videoId 漫画ID
 * @param series 章节序号（从1开始）
 * @param page 评论分页页码
 */
public record CommentQueryDataDto(
        int videoId,
        Integer series,
        int page
) {
    public CommentQueryDataDto(int videoId) {
        this(videoId, null, 1);
    }
}