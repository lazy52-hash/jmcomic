package com.lazy.jmcomic.api.v1.service;

import com.lazy.jmcomic.common.dto.SearchDto;
import com.lazy.jmcomic.common.pojo.AlbumDetail;
import com.lazy.jmcomic.common.pojo.AlbumInfo;
import com.lazy.jmcomic.common.pojo.AlbumPage;

import java.io.IOException;

/**
 * @author lazy
 * @since 2026.3.9
 * <p> 漫画服务</p>
 */
public interface IAlbumService {
    AlbumPage<AlbumInfo> findAlbum(SearchDto dto) throws IOException;
    AlbumDetail findAlbumById(int id) throws IOException;
}
