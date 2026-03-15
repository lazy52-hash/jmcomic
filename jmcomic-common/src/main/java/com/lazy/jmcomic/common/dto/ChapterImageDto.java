package com.lazy.jmcomic.common.dto;

/**
 * web版本章节DTO
 */
public record ChapterImageDto(
        String fileName,
        Integer scrambleId,
        Integer chapterId) {
}