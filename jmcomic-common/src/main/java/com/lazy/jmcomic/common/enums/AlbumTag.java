package com.lazy.jmcomic.common.enums;

/**
 * 漫画类型
 */
public enum AlbumTag {
    ALL_COMICS("","全部"),
    OTHER_COMICS("another", "其他"),
    DOUJIN_COMICS("doujin", "同人"),
    KOREAN_COMICS("hanman", "韩漫"),
    ENGLISH_COMICS("meiman", "Enginsh Manga"),
    SHORT_COMICS("short", "短篇"),
    SINGLE_COMICS("singe", "单本");

    private final String value;
    private final String description;

    AlbumTag(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static final AlbumTag DEFAULT = ALL_COMICS;
    public static final String QUERY_KEY = "/";
    public static AlbumTag getByValue(String value) {
        for (AlbumTag tag : values()) {
            if (tag.value.equals(value)) {
                return tag;
            }
        }
        return DEFAULT;
    }
}
