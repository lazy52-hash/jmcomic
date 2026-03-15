package com.lazy.jmcomic.api.v1.config.bean;

import com.lazy.jmcomic.api.v1.config.properties.ImageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Watermark {
    private final ImageProperties.Watermark watermark;
    public Watermark(ImageProperties imageProperties) {
        watermark=imageProperties.watermark();
    }
    @Autowired
    private TodayDate todayDate;
    private volatile String text;
    @PostConstruct
    public void updateWatermark(){
        refreshWatermark();
    }
    @Scheduled(cron = "1 0 0 * * ?")
    public void dailyRefresh() {
        refreshWatermark();
    }
    public String get(){
        return text;
    }
    private void refreshWatermark(){
        if(watermark.addDate()){
            text=String.format("%s | %s",watermark.value(),todayDate.get());
        }else{
            text=watermark.value();
        }
    }
    public boolean isEnabled(){
        return watermark.enabled();
    }
}
