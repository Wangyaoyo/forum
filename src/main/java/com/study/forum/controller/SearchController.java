package com.study.forum.controller;

import com.study.forum.pojo.DiscussPost;
import com.study.forum.service.ElasticSearchService;
import com.study.forum.service.LikeService;
import com.study.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索关键词
 * 记录： 被分页搞死
 * @author wy
 * @version 1.0
 */
@Controller
public class SearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;


    /**
     * 对关键词的搜索
     *
     * @param keyword
     * @param current
     * @param limit
     * @param model
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String getSearchPost(String keyword,
                                Integer current,
                                Integer limit, Model model) {
        Page<DiscussPost> discussPosts = elasticSearchService.searchPost(keyword, current, limit);
        List<Map<String, Object>> resList = new ArrayList<>();
        if (discussPosts != null && discussPosts.getTotalElements() > 0) {
            for (DiscussPost post : discussPosts.getContent()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.findUserLikeCount(post.getUserId()));
                resList.add(map);
            }
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("discussPosts", resList);
        model.addAttribute("page", discussPosts);
        System.out.println(discussPosts.getNumber());
        System.out.println(discussPosts.getTotalPages());
        System.out.println(discussPosts.getTotalElements());
        return "/site/search";
    }
}
