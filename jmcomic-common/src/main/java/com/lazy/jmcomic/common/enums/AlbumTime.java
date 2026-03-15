package com.lazy.jmcomic.common.enums;

/**
 * 漫画上传/更新时间
 */
public enum AlbumTime {
    ALL("","全部"),
    TODAY("t", "今日"),
    THIS_WEEK("w", "本周"),
    THIS_MONTH("m", "本月");

    private final String value;
    private final String description;

    AlbumTime(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static final AlbumTime DEFAULT = ALL;
    /**
     * 查询参数
     */
    public static final String QUERY_KEY = "t";
    public static AlbumTime getByValue(String value) {
        for (AlbumTime time : values()) {
            if (time.value.equals(value)) {
                return time;
            }
        }
        return DEFAULT;
    }

}
