package com.lazy.jmcomic.api.v2.pojo;

import java.util.Map;

/**
 * 请求封装对象
 */
public record ApiRequest(String path, Method method, Map<String,String> headers,Map<String,String> params,String data) {
    public enum Method{
        GET,
        POST
    }
}
