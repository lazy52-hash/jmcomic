package com.lazy.jmcomic.api.v1.service.impl;

import com.alibaba.fastjson.JSON;
import com.lazy.jmcomic.api.v1.client.WebCrawlFactory;
import com.lazy.jmcomic.api.v1.constant.fragment.CommentFragment;
import com.lazy.jmcomic.api.v1.constant.selector.ChapterPage;
import com.lazy.jmcomic.api.v1.service.IChapterService;
import com.lazy.jmcomic.common.dto.CommentQueryDataDto;
import com.lazy.jmcomic.common.pojo.ChapterInfo;
import com.lazy.jmcomic.common.pojo.Comment;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @since 2026.3.11
 * @author lazy
 * <p>漫画章节服务实现</p>
 */
@Slf4j
@Service
public class ChapterServiceImpl implements IChapterService {
    @Autowired
    private WebCrawlFactory factory;

    /**
     * 解析漫画章节
     * @param chapterId 章节id
     * @return
     * @throws IOException
     */
    @Override
    public ChapterInfo findChapterById(int chapterId) throws IOException {
        log.debug("解析漫画章节:{}", chapterId);
        Document document = factory.execute(cli -> cli.chapterPage(chapterId)).parse();
        String scriptText = document.select("script").toString();
        Elements photoCountElements = document.select(ChapterPage.PHOTO_COUNT_SELECTION);
        Elements commitCountElements = document.select(ChapterPage.COMMIT_COUNT_SELECTION);

        // 解析各字段
        int commitCount = 0;
        if (commitCountElements.size() == 2) {
            commitCount = parseInt(commitCountElements.first().text());
        }

        String[] photoNames = null;
        Matcher photoArrayMatcher = ChapterPage.PHOTO_IMAGE_ARRAY.matcher(scriptText);
        if (photoArrayMatcher.find()) {
            photoNames = JSON.parseObject(photoArrayMatcher.group(1), String[].class);
        }

        int photoCount = 0;
        if (!photoCountElements.isEmpty()) {
            Matcher photoCountMatcher = ChapterPage.PHOTO_COUNT.matcher(photoCountElements.first().text());
            if (photoCountMatcher.find()) {
                photoCount = parseInt(photoCountMatcher.group(1));
            }
        }

        Integer albumId = null;
        Matcher aidMatcher = ChapterPage.ALBUM_ID.matcher(scriptText);
        if (aidMatcher.find()) {
            albumId = parseInt(aidMatcher.group(1));
        }

        int scrambleId = 0;
        Matcher scrambleIdMatcher = ChapterPage.SCRAMBLE_ID.matcher(scriptText);
        if (scrambleIdMatcher.find()) {
            scrambleId = parseInt(scrambleIdMatcher.group(1));
        }

        ChapterInfo chapterInfo = new ChapterInfo(albumId, photoCount, commitCount, scrambleId, photoNames);
        log.debug("漫画章节解析成功: {}", chapterInfo);
        return chapterInfo;
    }

    /**
     * 将CDN头像URL转换为头像文件名
     */
    private String toAvatarGatewayPath(String cdnUrl) {
        if (cdnUrl == null || cdnUrl.isEmpty()) return "";
        String marker = "/media/users/";
        int idx = cdnUrl.indexOf(marker);
        if (idx >= 0) {
            return cdnUrl.substring(idx + marker.length());
        }
        log.warn("无法解析头像CDN URL: {}", cdnUrl);
        return "";
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 查询漫画章节评论
     * @param dto 评论查询参数
     * @return
     */
    @Override
    public List<Comment> findComment(CommentQueryDataDto dto) {
        log.debug("查询评论: videoId={}, series={}, page={}", dto.videoId(), dto.series(), dto.page());
        Connection.Response response = factory.execute(cli -> cli.commentPage(dto));
        if (response == null) {
            log.warn("评论请求返回空: videoId={}", dto.videoId());
            return Collections.emptyList();
        }
        try {
            // 响应体是HTML片段，用Jsoup解析
            Document doc = Jsoup.parse(response.body());
            Elements panels = doc.select(CommentFragment.COMMENT_PANEL);
            log.debug("解析到{}条评论", panels.size());

            List<Comment> comments = new ArrayList<>();
            for (Element panel : panels) {
                // 主评论timeline
                Element timeline = panel.selectFirst(CommentFragment.TIMELINE);
                if (timeline == null) continue;

                Comment comment = parseTimeline(timeline, true);
                comments.add(comment);
            }
            return comments;
        } catch (Exception e) {
            log.error("评论解析异常: videoId={}, message={}", dto.videoId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析单条评论timeline元素
     * @param timeline timeline元素
     * @param parseReplies 是否解析子回复
     * @return Comment对象
     */
    private Comment parseTimeline(Element timeline, boolean parseReplies) {
        String commentId = timeline.attr("data-cid").replace("{", "").replace("}", "");

        // 头像与用户ID，将CDN URL转为网关代理路径
        Element avatarEl = timeline.selectFirst(CommentFragment.AVATAR);
        String avatar = avatarEl != null ? toAvatarGatewayPath(avatarEl.attr("src")) : "";
        String userId = avatarEl != null ? avatarEl.attr("data-userid") : "";
        // 子回复的头像没有data-userid，从父级a标签href提取
        if (userId.isEmpty()) {
            Element userLink = timeline.selectFirst("div.timeline-left a[href]");
            if (userLink != null) {
                String href = userLink.attr("href");
                userId = href.substring(href.lastIndexOf("/") + 1);
            }
        }

        // 用户名、称号、等级
        Element usernameEl = timeline.selectFirst(CommentFragment.USERNAME);
        String username = usernameEl != null ? usernameEl.text().trim() : "";

        Element titleEl = timeline.selectFirst(CommentFragment.USER_TITLE);
        String userTitle = titleEl != null ? titleEl.text().trim().replaceFirst("^．", "") : "";

        Element levelEl = timeline.selectFirst(CommentFragment.USER_LEVEL);
        String userLevel = levelEl != null ? levelEl.text().trim() : "";

        // 日期、内容
        Element dateEl = timeline.selectFirst(CommentFragment.DATE);
        String date = dateEl != null ? dateEl.text().trim() : "";

        Element contentEl = timeline.selectFirst(CommentFragment.CONTENT);
        String content = contentEl != null ? contentEl.text().trim() : "";

        // 章节名
        Element chapterEl = timeline.selectFirst(CommentFragment.CHAPTER_LINK);
        String chapterName = chapterEl != null ? chapterEl.text().trim() : "";

        // 子回复
        List<Comment> replies = Collections.emptyList();
        if (parseReplies) {
            Elements replyElements = timeline.select(CommentFragment.REPLIES);
            if (!replyElements.isEmpty()) {
                replies = new ArrayList<>();
                for (Element reply : replyElements) {
                    replies.add(parseTimeline(reply, false));
                }
            }
        }

        return new Comment(commentId, userId, username, userTitle, userLevel,
                avatar, date, content, chapterName, replies);
    }
}