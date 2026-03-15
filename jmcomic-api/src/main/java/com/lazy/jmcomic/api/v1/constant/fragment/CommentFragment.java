package com.lazy.jmcomic.api.v1.constant.fragment;

/**
 * 评论HTML片段CSS选择器
 */
public class CommentFragment {
    private CommentFragment() {}

    /** 每条主评论的容器 */
    public static final String COMMENT_PANEL = "div.panel.timeline-panel";
    /** 评论主体（含data-cid） */
    public static final String TIMELINE = "div.timeline[data-cid]";
    /** 用户头像 */
    public static final String AVATAR = "img.timeline-avatar";
    /** 用户等级 */
    public static final String USER_LEVEL = "div.timeline-user-level";
    /** 用户显示名 */
    public static final String USERNAME = "span.timeline-username";
    /** 用户称号 */
    public static final String USER_TITLE = "div.timeline-user-title";
    /** 评论日期 */
    public static final String DATE = "div.timeline-date";
    /** 评论内容 */
    public static final String CONTENT = "div.timeline-content";
    /** 章节链接 */
    public static final String CHAPTER_LINK = "div.timeline-ft a small";
    /** 子回复区域 */
    public static final String REPLIES = "div.other-timelines > div.timeline";
    /** "查看更多"按钮（判断是否有下一页） */
    public static final String LOAD_MORE = "a[id^=p_album_comments_]";
}