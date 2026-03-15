package com.lazy.jmcomic.api.v1.config.bean;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TodayDate {
    private String date = "";
    @PostConstruct
    @Scheduled(cron = "0 0 0 * * ?")  // 每天00:00刷新
    public void update() {
        date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    }
    public String get() {
        return date;
    }
}