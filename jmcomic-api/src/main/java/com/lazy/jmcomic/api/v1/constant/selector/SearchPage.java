package com.lazy.jmcomic.api.v1.constant.selector;

import java.util.regex.Pattern;

public final class SearchPage {
    private SearchPage() {}
    /** 匹配漫画跳转url和id的表达式 */
    public static final Pattern URL_COMIC_ID=Pattern.compile("\\s*/album/(\\d+)/\\s*");
    /** 提取漫画的跳转链接,用于从里面提取ID */
    public static final String COMIC_HREF_SELECTION=".thumb-overlay>a";
    /** 漫画是否有更新标签*/
    public static final String COMIC_IS_UPDATED_SELECTION=".label-latest>.label-category";
    /** 漫画类型(第一个label 黑灰色容器那个) */
    public static final String COMIC_TYPE_SELECTION=".category-icon>.label-category";
    /** 漫画标签,第一个灰色label右边那个 */
    public static final String COMIC_TAG_SELECTION=".category-icon>.label-sub";
    /** 漫画封面图 */
    public static final String COMIC_COVER_SELECTION=".lazy_img.img-responsive.img-rounded";
    /** 漫画的点赞数量,其实文字容器可以选择.albim_likes_,但是因为他那个命名错了我认为后续应该会改掉,不选择这个 */
    public static final String COMIC_LIKES_SELECTION="[id^=love_likes_]>.text-white";
    /** 漫画名称 为何类名是video? */
    public static final String COMIC_NAME_SELECTION=".video-title.title-truncate";
    /** 作者名称 */
    public static final String COMIC_AUTHORS_SELECTION=".title-truncate>a[href*='/search/photos?'][class!='tag']";
    /** 提取漫画的标签列表 */
    public static final String COMIC_TAGS_SELECTION=".tags>.tag";
    //搜索页一些其他数据
    public static final String COMIC_ELEMENTS_SELECTION=".well:not(.well-sm):not(.well-filters)";
    public static final String COMIC_SEARCH_MESSAGE_SELECTION=".well.well-sm";
    public static final String COMIC_TOTAL_PAGES_SELECTION=".pagination>li:last-child";
    public static final String COMIC_TOTAL_ELEMENTS_SELECTION=".well.well-sm>.text-white:last-child";
}
