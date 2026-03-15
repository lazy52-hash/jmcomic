package com.lazy.jmcomic.common.enums;

/**
 * 搜索类型
 */
public enum AlbumType {
    ALL(0, "全部"),
    BY_AUTHOR(1, "根据作者"),
    BY_COMIC(2, "根据作品"),
    BY_TAG(3, "根据标签"),
    BY_ACTOR(4, "根据人物");

    private final int value;
    private final String description;

    AlbumType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static final AlbumType DEFAULT = ALL;
    public static final String QUERY_KEY = "main_tag";
    public static AlbumType getByValue(Integer value) {
        for (AlbumType type : values()) {
            if (type.value==value) {
                return type;
            }
        }
        return DEFAULT;
    }
}
