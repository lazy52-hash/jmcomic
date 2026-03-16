package com.lazy.jmcomic.api.v1.service.impl;

import com.lazy.jmcomic.api.v1.client.ImageDownloadClient;
import com.lazy.jmcomic.api.v1.config.bean.Watermark;
import com.lazy.jmcomic.api.v1.constant.MagicConstants;
import com.lazy.jmcomic.api.v1.constant.WebImage;
import com.lazy.jmcomic.api.v1.service.ImageService;
import com.lazy.jmcomic.common.dto.ChapterImageDto;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private Watermark watermark;
    @Autowired
    private Font font;
    @Autowired
    private ImageDownloadClient downloadClient;

    /**
     * 下载漫画封面
     * @param filename 漫画cover文件名
     * @return
     */
    @Override
    public Mono<byte[]> albumCover(String filename) {
        return downloadClient.download(String.format(WebImage.COMIC_COVER,filename));
    }

    /**
     * 下载GIF图片（GIF不加密，直接下载透传）
     * @param chapterId 章节ID
     * @param fileName 文件名（无后缀）
     * @return GIF图片字节
     */
    @Override
    public Mono<byte[]> downloadGif(int chapterId, String fileName) {
        log.debug("下载GIF图片: chapterId={}, fileName={}", chapterId, fileName);
        return downloadClient.download(String.format(WebImage.COMIC_IMAGE, chapterId, fileName + ".gif"));
    }

    /**
     * 下载用户头像
     * @param filename 头像文件名（含后缀，如 abc123.jpg）
     * @return 头像图片字节
     */
    @Override
    public Mono<byte[]> userAvatar(String filename) {
        log.debug("下载用户头像: filename={}", filename);
        return downloadClient.download(String.format(WebImage.USER_AVATAR, filename));
    }

    /**
     * 解码章节图片
     * @param image 章节图片对象
     * @return
     */
    @Override
    public Mono<byte[]> decodeImage(ChapterImageDto image) {
        int num=calcSegmentationNum(image.scrambleId(),image.chapterId(),image.fileName());
        return downloadClient.download(String.format(WebImage.COMIC_IMAGE,image.chapterId(),image.fileName()+".webp")).publishOn(Schedulers.boundedElastic())
                .map(bytes -> {
                    try {
                        ImmutableImage src = ImmutableImage.loader().fromBytes(bytes);
                        BufferedImage srcBuf = src.toNewBufferedImage(BufferedImage.TYPE_INT_ARGB);
                        BufferedImage dst = watermark.isEnabled()?addSubtleWatermark(decode(srcBuf, num),watermark.get()):decode(srcBuf, num);

                        if (!dst.getColorModel().hasAlpha()) {
                            BufferedImage alphaImg = new BufferedImage(dst.getWidth(), dst.getHeight(), BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2 = alphaImg.createGraphics();
                            g2.drawImage(dst, 0, 0, null);
                            g2.dispose();
                            dst = alphaImg;
                        }
                            //太慢了
    /*                        int w = dst.getWidth();
                            int h = dst.getHeight();
                            int type = dst.getType();
                            int[] pixels = new int[w * h];
                            dst.getRGB(0, 0, w, h, pixels, 0, w);
                            ImmutableImage target = ImmutableImage.create(w, h, type);
                            target.awt().setRGB(0, 0, w, h, pixels, 0, w);
                            WebpWriter losslessWriter = WebpWriter.DEFAULT
                                    .withLossless()
                                    .withQ(100)
                                    .withM(6);
                            return target.bytes(losslessWriter);*/
                            //慢
                            /*
                            java.lang.reflect.Method wrapMethod = ImmutableImage.class.getDeclaredMethod("wrapAwt", BufferedImage.class);
        wrapMethod.setAccessible(true);
        ImmutableImage target = (ImmutableImage) wrapMethod.invoke(null, dst);

        WebpWriter fastLossless = WebpWriter.DEFAULT
            .withLossless()
            .withQ(75)   // 无损下 Q 越低努力越小，文件稍大但速度快很多
            .withM(4);   // 或 M=3 / M=2（M=4 平衡点）

        return target.bytes(fastLossless);
                             */
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 256);
                        boolean success = ImageIO.write(dst, "webp", bos);
                        if (!success) {
                            log.warn("WebP 写入失败，使用 PNG 后备");
                            bos.reset();
                            ImageIO.write(dst, "png", bos);
                        }
                        return bos.toByteArray();
                    } catch (IOException e) {
                        throw new RuntimeException("图像解码/写入失败", e);
                    }
                });
    }
    /**
     * function onImageLoadedForNew(e){if("true"!==e.dataset.scrambleProcessed){var t,a=e.nextElementSibling,o;if(a&&"CANVAS"===a.tagName)(o=(t=a).getContext("2d")).clearRect(0,0,t.width,t.height);else(t=document.createElement("canvas")).style.display="block",e.after(t);var o=t.getContext("2d"),d=e.width,r=e.naturalWidth,s=e.naturalHeight;if(0===r||0===s)return console.warn("圖片尺寸異常，延遲處理:",e.id),void setTimeout(function(){e.dataset.scrambleProcessed||onImageLoadedForNew(e)},100);t.width=r,t.height=s,(d>e.parentNode.offsetWidth||0==d)&&(d=e.parentNode.offsetWidth),t.style.width=d+"px";var n=e.parentNode.id||e.id;n=n.split(".")[0];var i,l=$(e).attr("data-chapter-aid")||aid;n.includes("next")&&(n=n.split("_").pop());try{for(var c=get_num(window.btoa(l),window.btoa(n)),f=parseInt(s%c),m=r,g=0;g<c;g++){var h=Math.floor(s/c),u=h*g,p=s-h*(g+1)-f;0==g?h+=f:u+=f,o.drawImage(e,0,p,m,h,0,u,m,h)}e.dataset.scrambleProcessed="true",$(e).addClass("hide")}catch(t){console.error("圖片重組失敗:",t,e.id),e.style.display="block",e.dataset.scrambleProcessed="true"}}}'
     * @param src
     * @param num
     * @return
     */
    public BufferedImage decode(BufferedImage src, int num) {
        if (num == 0) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        int over = h % num;
        int move = (int) Math.floor((double) h / num);
        for (int i = 0; i < num; i++) {
            int ySrc = h - (move * (i + 1)) - over;
            int yDst = move * i;
            int blockH = move;
            if (i == 0) {
                blockH += over;
            } else {
                yDst += over;
            }
            BufferedImage part = src.getSubimage(0, ySrc, w, blockH);
            g.drawImage(part, 0, yDst, null);
        }
        g.dispose();
        return dst;
    }
    /**
     * 计算分块
     * @param scrambleId
     * @param chapterId 章节ID
     * @param fileName 文件名(无后缀)
     * @return 分块数量
     */
    private int calcSegmentationNum(int scrambleId, int chapterId, String fileName) {
        if (chapterId < scrambleId) {
            return 0;
        } else if (chapterId < MagicConstants.SCRAMBLE_268850) {
            return 10;
        } else {
            int x = chapterId < MagicConstants.SCRAMBLE_421926 ? 10 : 8;
            String s = chapterId + fileName;
            String md5 = md5Hex(s);
            int num = md5.charAt(md5.length() - 1);
            num %= x;
            num = num * 2 + 2;
            return num;
        }
    }
    private String md5Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    private  BufferedImage addSubtleWatermark(BufferedImage srcImage, String watermark) {
        if (srcImage == null || watermark == null || watermark.trim().isEmpty()) {
            return srcImage;
        }
        int fontSize = Math.min(20, srcImage.getWidth() / 20);

        Graphics2D g2d = srcImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout layout = new TextLayout(watermark, font, frc);
            Rectangle2D bounds = layout.getBounds();
            double margin = fontSize * 0.6;
            float x = (float) (srcImage.getWidth() - bounds.getWidth() - margin);
            float y = (float) (srcImage.getHeight() - margin - layout.getDescent());  // 基线位置，防下伸部裁剪
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            Shape textShape = layout.getOutline(at);

            // 灰色描边
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(
                    fontSize * 0.02f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g2d.draw(textShape);
            g2d.setColor(new Color(255, 255, 255, 55));
            g2d.fill(textShape);

        } finally {
            g2d.dispose();
        }
        return srcImage;
    }
}
