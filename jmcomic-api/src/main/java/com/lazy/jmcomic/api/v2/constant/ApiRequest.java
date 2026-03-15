package com.lazy.jmcomic.api.v2.constant;

import java.util.Map;

public class ApiRequest {
    public static final String[] DOMAINS= {
            "www.cdnhth.net",
            "www.cdnhth.club",
            "www.cdnhth.cc",
            "www.cdngwc.net",
            "www.cdngwc.club",
            "www.cdngwc.cc",
            "www.cdnhjk.cc"
    };
    public static final Map<String,String> HEADERS=Map.of(
            "Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-language","zh-CN,zh;q=0.9,zh-TW;q=0.8,zh-HK;q=0.7,en-US;q=0.6,en;q=0.5",
            "User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0"
    );}
