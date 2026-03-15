package com.lazy.jmcomic.api.v1.service;

import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.pojo.ChapterInfo;
import com.lazy.jmcomic.common.pojo.Comment;

import java.io.IOException;
import java.util.List;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>章节服务</p>
 */
public interface IChapterService {
    ChapterInfo findChapterById(int id) throws IOException;

    /**
     * 查询章节评论
     * @param dto 评论查询参数
     * @return 评论列表
     */
    List<Comment> findComment(CommentQueryDataDto dto);
}