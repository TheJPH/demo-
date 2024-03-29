package com.example.community.controller;

import com.example.community.cache.HotTagCache;
import com.example.community.dto.PaginationDTO;
import com.example.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class IndexController {


    @Autowired
    private QuestionService questionService;
    @Autowired
    private HotTagCache hotTagCache;

    /*首页分页
     * 偏移page 条目数size,
     * 搜索功能
     *根据搜索结果显示页面
     * */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "5") Integer size,
                        @RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "tag", required = false) String tag) {

        PaginationDTO pagination = questionService.list(search,tag, page, size);
        List<String> tags = hotTagCache.getHots();
        model.addAttribute("pagination", pagination);
        if (StringUtils.isNotBlank(search)){
            model.addAttribute("search", search);
        }else{
            model.addAttribute("search",null);
        }
        model.addAttribute("tag", tag);
        model.addAttribute("tags", tags);
        return "index";
    }
}
