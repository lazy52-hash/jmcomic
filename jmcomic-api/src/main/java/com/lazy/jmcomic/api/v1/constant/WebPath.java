package com.lazy.jmcomic.api.v1.constant;

/**
 * <p>web爬取页面基础路径</p>
 */
public final class WebPath {
    private WebPath() {}
    /**
     * 占位符:
     * <ul>
     *     <li>漫画ID</li>
     * </ul>
     * */
    public static final String COMIC_ALBUM="/album/%d";
    /**
     * 占位符:
     * <ul>
     *     <li>章节ID</li>
     * </ul>
     * */
    public static final String COMIC_CHAPTER="/photo/%d";

    public static final String COMIC_SEARCH="/search/photos";

}
