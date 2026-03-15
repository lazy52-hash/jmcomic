package com.lazy.jmcomic.api.v1.constant.selector;

import java.util.regex.Pattern;

public final class ChapterPage {
    private ChapterPage() {}
    public static final Pattern ALBUM_ID = Pattern.compile("var aid = (\\d+);");
    public static final Pattern SCRAMBLE_ID = Pattern.compile("var scramble_id = (\\d+);");
    public static final Pattern PHOTO_COUNT=Pattern.compile("\\s*1\\s*/\\s*(\\d+)");
    public static final Pattern PHOTO_IMAGE_ARRAY=Pattern.compile("var\\s+page_arr\\s*=\\s*(\\[[^\\]]*?\\])\\s*;");
    public static final String PHOTO_COUNT_SELECTION="div#page_0";
    public static final String COMMIT_COUNT_SELECTION=".menu-bolock-ul>li:first-child>a>span";

}
