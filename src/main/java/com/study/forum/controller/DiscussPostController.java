package com.study.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.pojo.User;
import com.study.forum.service.DiscussPostService;
import com.study.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wy
 * @version 1.0
 */
@Controller
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String pageData(@RequestParam(value = "current",required = false) Integer current, Model model) {
        Page<DiscussPost> page = discussPostService.getPageDiscussPosts(0, current, 10);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(page != null){
            for (DiscussPost post : page.getRecords()){
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("post", post);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("page", page);
        return "index";
    }

    @GetMapping("/discussposts/{userId}")
    public int getRowNumbers(@PathVariable("userId") int userId) {
        return discussPostService.getDiscussPostRows(userId);
    }
}
