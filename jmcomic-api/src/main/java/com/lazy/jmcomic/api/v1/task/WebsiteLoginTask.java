package com.lazy.jmcomic.api.v1.task;

import com.lazy.jmcomic.api.v1.client.WebCrawlClient;
import com.lazy.jmcomic.api.v1.client.WebCrawlFactory;
import com.lazy.jmcomic.api.v1.config.properties.DevProperties;
import com.lazy.jmcomic.api.v1.config.properties.LoginProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class WebsiteLoginTask {
    @Autowired
    private LoginProperties loginProperties;
    @Autowired
    private DevProperties devProperties;
    @Autowired
    private ConcurrentHashMap<String, String> cookies;
    @Autowired
    private WebCrawlFactory factory;
    @PostConstruct
    @Scheduled(cron = "0 0 9 * * Mon")
    public void run(){
        //开发环境检测后检测是否配置了开发环境使用的cookies
        if(!devProperties.isEmpty()&&!cookies.isEmpty()){
          log.info("当前已配置开发cookies,阻止默认登录逻辑");
            return;
        };
        Connection.Response response =factory.execute(webCrawlClient -> {
            try {
                return loginRetry(0,webCrawlClient);
            } catch (IOException e) {
                log.error("登录异常:{}",e.getMessage());
                return null;
            }
        });
        if(response==null){
            log.warn("登录失败，跳过Cookie设置");
            return;
        }
        Map<String,String> cks = response.cookies();
        if(!cks.isEmpty()){
            log.info("登录成功:{}",cks.keySet().stream().reduce((str,k)-> str + String.format("%s=%s;", k, cks.get(k))).get());
            cookies.putAll(cks);
        }else{
            log.warn("无法获取cookies");
        }
    }
    public Connection.Response loginRetry(int i, WebCrawlClient client) throws IOException {
        Connection.Response r=client.loginApi(loginProperties.username(),loginProperties.password());
        if (r != null && (r.statusCode() != HttpURLConnection.HTTP_OK || r.cookies().isEmpty())) {
            if (i < 3) {
                log.warn("登录异常-code:{},message:{}", r.statusCode(), r.statusMessage());
                return loginRetry(++i, client);
            } else {
                return null;
            }
        }else if(r== null){
            if (i < 3) {
                return loginRetry(++i, client);
            } else {
                return null;
            }
        }
        return r;
    }
}
