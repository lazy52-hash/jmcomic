package com.lazy.jmcomic.api.v1.controller;

import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v1.service.IChapterService;
import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.pojo.ChapterInfo;
import com.lazy.jmcomic.common.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @since 2026.3.11
 * @author lazy
 * <p>章节控制器</p>
 */
@JMController.v1
public class ChapterController {
    @Autowired
    private IChapterService service;
    @GetMapping("/chapter/{id}")
    public ChapterInfo chapterInfo(@PathVariable Integer id) throws IOException {
        return service.findChapterById(id);
    }
    @PostMapping("/chapter/comment")
    public List<Comment> comment(@RequestBody CommentQueryDataDto dto) throws IOException {
        return service.findComment(dto);
    }
}
