package com.lazy.jmcomic.api.v1.constant;

import java.util.List;
import java.util.Map;

/**
 * <p>网站配置</p>
 */
public final class Website {
    private  Website() {
    }
    public static final String PROT="https://";
    public static final String[] DOMAINS= {
            "18comic.vip",
            "18comic.ink",
            "jmcomic-zzz.one",
            "jmcomic-zzz.org",
            "jmcomic.me",
            "jmcomic1.me"
    };
    public static final Map<String,String> HEADERS=Map.of(
            "Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-language","zh-CN,zh;q=0.9,zh-TW;q=0.8,zh-HK;q=0.7,en-US;q=0.6,en;q=0.5",
            "User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0"
    );

    public static final String[] IMAGES_CDN_ARRAY={"cdn-msp","cdn-msp2","cdn-msp3"};




}
