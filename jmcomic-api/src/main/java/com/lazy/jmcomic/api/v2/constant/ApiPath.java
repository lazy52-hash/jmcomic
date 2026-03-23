package com.lazy.jmcomic.api.v2.constant;

public final class ApiPath {
    private ApiPath(){}

    /**jm设定*/
    public static final String API_APP_SETTING="setting";

    //漫画区
    /**漫画搜索*/
    public static final String API_COMIC_SEARCH="search";
    /**热门话题*/
    public static final String API_COMIC_HOT_TAGS="hot_tags";
    /**随机推荐*/
    public static final String API_COMIC_RANDOM_RECOMMEND="random_recommend";
    /**漫画详情*/
    public static final String API_COMIC_ALBUM="album";
    /**章节信息*/
    public static final String API_COMIC_CHAPTER="chapter";
    /** 下载页*/
    public static final String API_ALBUM_DOWNLOAD="album_download_2";
    /**阅读/返回图片cdn数据+常规章节数据*/
    public static final String API_COMIC_READ="comic_read";
    /**评论列表接口 */
    public static final String API_COMIC_COMMENT="forum";

    /** 每周必看*/
    public static final String API_WEEK="week";

    //小说
    /** 应该是一键查询小说列表,但是为什么传入与查询detail一样的参数 */
    public static final String API_NOVEL_LIST="novels";
    /** 搜索小说*/
    public static final String API_NOVEL_SEARCH="search_novels";
    /** 查询小说详情 */
    public static final String API_NOVEL_DETAIL="novel";
    /** 获取小说内容  */
    public static final String API_NOVEL_CHAPTERS="novelchapters";

    public static final String LOGIN="login";
}
