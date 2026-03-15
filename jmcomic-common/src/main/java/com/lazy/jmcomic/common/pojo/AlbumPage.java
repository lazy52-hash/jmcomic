package com.lazy.jmcomic.common.pojo;

import java.util.List;

/**
 * Page对象
 * 由于JM似乎没有pageSize调整?暂时不加了
 * @param <T>
 */
public record AlbumPage<T>(
        List<T> items,
        int currentPage,
        int totalElements,
        int totalPages,
        String message) {

    public AlbumPage(String message) {
        this(null, 0, 0, 0, message);
    }

    public AlbumPage() {
        this(null);
    }
}