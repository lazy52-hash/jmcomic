package com.lazy.jmcomic.api.v1.controller;

import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v1.service.IAlbumService;
import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.pojo.AlbumDetail;
import com.lazy.jmcomic.common.pojo.AlbumInfo;
import com.lazy.jmcomic.common.pojo.AlbumPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author lazy
 * @since 2026.3.11
 * <p>漫画控制器</p>
 */
@JMController.v1
public class AlbumController {
    @Autowired
    private IAlbumService service;
    @PostMapping("/album/search")
    public AlbumPage<AlbumInfo> search(@RequestBody SearchDto dto) throws IOException {
        return service.findAlbum(dto);
    }
    @GetMapping("/album/{id}")
    public AlbumDetail albumDetail(@PathVariable(value = "id") Integer albumId) throws IOException {
        return service.findAlbumById(albumId);
    }
}
