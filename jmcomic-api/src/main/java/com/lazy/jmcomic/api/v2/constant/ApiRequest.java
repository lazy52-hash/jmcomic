package com.lazy.jmcomic.api.v2.constant;

import java.util.Map;

public class ApiRequest {
    public static final String[] DOMAINS= {
            "www.cdngwc.club",
            "www.cdnhth.club",
            "www.cdnhth.net",
            "www.cdnhth.cc",
            "www.cdngwc.net",
            "www.cdngwc.cc",
            "www.cdnhjk.cc"
    };
    //jm客户端请求头
    public static final Map<String,String> HEADERS=Map.of(
            "Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-language","zh-CN,zh;q=0.9,zh-TW;q=0.8,zh-HK;q=0.7,en-US;q=0.6,en;q=0.5",
            "User-Agent","Mozilla/5.0 (Linux; Android 12; NCO-AL00 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/110.0.5481.154 Mobile Safari/537.36"
    );}
