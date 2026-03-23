package com.lazy.jmcomic.api.v2.controller;

import com.alibaba.fastjson2.JSONObject;
import com.lazy.jmcomic.api.v2.client.ApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private ApiClientFactory factory;
    @Autowired
    private WebClient zipDownloadClient;
    @GetMapping("/redirect/{id}.zip")
    public Mono<ResponseEntity<Void>> download(@PathVariable int id){
        return factory.downloadPage(id)
                .flatMap(s -> {
                    JSONObject obj = JSONObject.parseObject(s);
                    String downloadUrl = obj.getString("download_url");
                    return Mono.just(ResponseEntity.status(HttpStatus.FOUND).header("Location", downloadUrl).build());
                });
    }

    @GetMapping("/proxy/{id}.zip")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadProxy(@PathVariable int id) {
        return factory.downloadPage(id)
                .flatMap(s -> {
                    try {
                        JSONObject obj = JSONObject.parseObject(s);
                        String downloadUrl = obj.getString("download_url");
                        log.info("开始下载: {}, URL: {}", id, downloadUrl);
                        // 流式下载文件
                        Flux<DataBuffer> dataBufferFlux = zipDownloadClient
                                .method(HttpMethod.GET)
                                .uri(downloadUrl)
                                .accept(MediaType.APPLICATION_OCTET_STREAM)
                                .retrieve()
                                .bodyToFlux(DataBuffer.class);

                        // 构建响应
                        return Mono.just(ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\"" + id + ".zip\"")
                                .body(dataBufferFlux));

                    } catch (Exception e) {
                        log.error("解析下载信息失败", e);
                        return Mono.error(e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("下载失败", e);
                    return Mono.just(ResponseEntity.status(500)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(Flux.just(DefaultDataBufferFactory.sharedInstance
                                    .wrap(("下载失败: " + e.getMessage())
                                            .getBytes(StandardCharsets.UTF_8)))));
                });
    }
}
