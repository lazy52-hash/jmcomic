package com.lazy.jmcomic.common.enums;

/**
 * 搜索类型排序
 */
public enum SortType {
    BY_VIEWS_DESC("mv", "最多点阅的"),
    BY_IMAGES_DESC("mp", "最多图片的"),
    BY_LIKES_DESC("tf", "最多爱心的"),
    BY_TIME_DESC("mr", "最新的");

    private final String value;
    private final String description;

    SortType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static final SortType DEFAULT = BY_TIME_DESC;
    public static final String QUERY_KEY = "o";
    public static SortType getByValue(String value) {
        for (SortType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
