package com.lazy.jmcomic.api.common.util;

import com.lazy.jmcomic.api.v1.constant.Website;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lazy
 * @since 2026.3.8
 * <p>httping工具</p>
 */
@Slf4j
public final class HttpPingUtil {
    private HttpPingUtil(){}

    /**
     *
     * @param url url
     * @param proxy 代理对象
     * @param headers 请求头
     * @param timeout 连接超时时间
     * @return
     */
    public static boolean isAlive(String url, Map<String,String> headers,Proxy proxy, int timeout) throws IOException {
        if(url==null||url.isBlank()){
            throw new IllegalArgumentException("URL不能为空");
        }
        URL u=new URL(url);
        HttpURLConnection uc = null;
        try{
           log.debug("httping: {}",url);
           uc=(HttpURLConnection) u.openConnection(proxy==null?Proxy.NO_PROXY:proxy);
           uc.setConnectTimeout(timeout);
           uc.setReadTimeout(timeout);
           uc.setRequestMethod("GET");
           uc.setRequestProperty("Range", "bytes=0-0");
           Website.HEADERS.forEach(uc::setRequestProperty);
           if(headers!=null&&!headers.isEmpty()){
               headers.forEach(uc::setRequestProperty);
           }
           uc.connect();
           int responseCode = uc.getResponseCode();
           if(responseCode/100==2){
               log.debug("httping 成功: {} -> {}",url,responseCode);
               return true;
           }
           log.warn("httping 失败: {} -> HTTP {} {}",url,responseCode,uc.getResponseMessage());
           return false;
        }catch (MalformedURLException e){
           log.error("httping 失败: {} -> URL格式错误: {}",url,e.getMessage());
           return false;
        }catch (SocketTimeoutException e){
           log.warn("httping 超时: {} -> {}ms未响应",url,timeout);
           return false;
        }catch (ConnectException e){
           log.warn("httping 连接被拒: {} -> {}",url,e.getMessage());
           return false;
        }catch (IOException e){
           log.warn("httping IO异常: {} -> {} {}",url,e.getClass().getSimpleName(),e.getMessage());
           return false;
       }finally {
           if(uc!=null){uc.disconnect();}
       }
    }

    public static Map<String,String> resolveHeaders(Map<String,String> configured,String domain,String baseurl){
        Map<String,String> headers=new HashMap<>();
        if(configured!=null){
            configured.forEach((k,v)->{
                if(v==null||v.isBlank()){
                    switch(k){
                        case "Host"->headers.put(k,domain);
                        case "Referer"->headers.put(k,baseurl+"/");
                    }
                }else{
                    headers.put(k,v);
                }
            });
        }
        return headers;
    }
}
