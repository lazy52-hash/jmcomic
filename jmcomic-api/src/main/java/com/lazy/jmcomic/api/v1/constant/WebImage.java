package com.lazy.jmcomic.api.v1.constant;

/**
 * <p>web图片资源</p>
 */
public final class WebImage {
    private WebImage() {}
    /** 占位符:
     * <ul>
     *     <li>章节ID</li>
     *     <li>文件名</li>
     * </ul>
     * */
    public static final String COMIC_IMAGE="/media/photos/%d/%s";
    /** 占位符:
     * <ul>
     *     <li>漫画ID</li>
     * </ul>
     * */
    public static final String COMIC_COVER="/media/albums/%d_3x4.jpg";
    /** 占位符:
     * <ul>
     *     <li>用户头像文件名（含后缀）</li>
     * </ul>
     * */
    public static final String USER_AVATAR="/media/users/%s";

    //默认测试CDN连通性图片路径

}
