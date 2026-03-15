package com.lazy.jmcomic.common.pojo;

import java.util.List;

/**
 * 漫画章节评论
 * @param commentId 评论ID（data-cid）
 * @param userId 用户ID
 * @param username 用户显示名
 * @param userTitle 用户称号
 * @param userLevel 用户等级
 * @param avatar 头像URL
 * @param date 评论日期
 * @param content 评论内容
 * @param chapterName 所属章节名（如"第1話"）
 * @param replies 子回复列表
 */
public record Comment(
        String commentId,
        String userId,
        String username,
        String userTitle,
        String userLevel,
        String avatar,
        String date,
        String content,
        String chapterName,
        List<Comment> replies
) {
}