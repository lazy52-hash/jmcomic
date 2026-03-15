package com.lazy.jmcomic.api.v1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * 水印字体配置
 */
@Slf4j
@Configuration
public class FontConfig {
    @Bean
    public Font font(){
        Font font = null;
        try (InputStream is = getClass().getResourceAsStream("/fonts/LXGWWenKaiMonoTC-Light.ttf")) {
            if (is == null) {
                log.warn("字体文件不存在");
                throw new IOException("字体文件不存在");
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            font = baseFont.deriveFont(Font.ITALIC,20);

        } catch (Exception e) {
            log.warn("字体文件不存在,采用默认字体");
            font = new Font(Font.SANS_SERIF, Font.ITALIC, 20);
        }
        log.info("字体加载成功: {}", font.getFontName());
        return font;
    }
}
