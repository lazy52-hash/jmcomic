package com.lazy.jmcomic.api.v1.constant.selector;

public final class AlbumDetailPage {
    private AlbumDetailPage() {}

    /**
     * 漫画名称
     */
    public static final String NAME_SELECTION = "h1#book-name";

    public static final String COVER_SELECTION = ".thumb-overlay>.lazy_img.img-responsive";

    public static final  String ACTORS_SELECTION="span[data-type='actor']>a";

    public static final String TAGS_SELECTION="[data-type='tags']>a";

    public static final String AUTHORS_SELECTION="[data-type='author']>a";

    public static final String DETAIL_CONTAINER_SELECTION=".col-lg-7>div[class='']";
    /** 包含漫画的核心数据 */
    public static final String DETAIL_SELECTIONS=".p-t-5.p-b-5";

    /** 评论数量 */
    public static final String COMMITS_SELECTION="div#total_video_comments";
    /** 章节列表 */
    public static final String CHAPTER_SELECTION=".btn-toolbar";
    /** 点赞 */
    public static final String LIKES_SELECTION="[id^='albim_likes_']";
    /** 漫画时间 */
    public static final String TIME_SELECTION="[itemprop='datePublished']";

    public static final String WORKS_SELECTION="[data-type='works']>a";

}
