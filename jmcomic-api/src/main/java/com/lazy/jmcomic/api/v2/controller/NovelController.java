package com.lazy.jmcomic.api.v2.controller;

import com.lazy.jmcomic.api.common.annotation.JMController;
import com.lazy.jmcomic.api.v2.client.ApiClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * <p>小说控制器</p>
 * @since 2026.3.15
 * @author 3.15
 */
@JMController.v2
public class NovelController {
    @Autowired
    private ApiClientFactory factory;
    @GetMapping("/novel/search")
    public Mono<String> search(@RequestParam String keyword){
        return factory.novelSearch(keyword);
    }
    @GetMapping("/novel/list")
    public Mono<String> list(@RequestParam(required = false,defaultValue = "") String o,
                             @RequestParam(required = false,defaultValue = "") String t,
                             @RequestParam(required = false,defaultValue = "") Integer id){
        return factory.novelList(o,t,id);
    }
    @GetMapping("/novel/{nid}")
    public Mono<String> detail(@PathVariable int nid){
        return factory.novelDetail(nid);
    }
    @GetMapping("/novel/chapter/{cid}")
    public Mono<String> read(@PathVariable int cid,@RequestParam(required = false,defaultValue = "tw") String lang){
        return factory.novelChapter(cid,lang);
    }

}
