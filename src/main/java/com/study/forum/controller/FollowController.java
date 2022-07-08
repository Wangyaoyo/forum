package com.study.forum.controller;

import com.study.forum.pojo.User;
import com.study.forum.service.FollowService;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Controller
public class FollowController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        Integer userId = hostHolder.getUser().getId();
        followService.follow(userId, entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    /**
     * 某人（当前用户）取消对某实体的关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        Integer userId = hostHolder.getUser().getId();
        followService.unfollow(userId, entityType, entityId);
        return CommunityUtil.getJSONString(0, "取消关注");
    }

    /**
     * 查询userId的关注列表
     * （此处的userId不一定是hostHolder）
     *
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(value = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") Integer userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("该用户不存在");
        }
        List<Map<String, Object>> users = followService.findFollowees(userId);
        // 遍历每一个用户，看当前用户是否关注了某用户
        if (users != null) {
            for (Map<String, Object> map : users){
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        // xxx的关注或粉丝
        model.addAttribute("users", users);
        // 某用户自身的信息
        model.addAttribute("user", user);
        return "/site/followee";
    }

    /**
     * 查询userId的粉丝列表
     * （此处的userId不一定是hostHolder）
     *
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(value = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") Integer userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("该用户不存在");
        }
        List<Map<String, Object>> followers = followService.findFollowers(userId);
        // 遍历每一个用户，看当前用户是否关注了某用户
        if (followers != null) {
            for (Map<String, Object> map : followers){
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followers);
        model.addAttribute("user", user);
        return "/site/follower";
    }


    /**
     * 查看当前用户是否关注了userId
     * @param userId
     * @return
     */
    public boolean hasFollowed(int userId){
        if(hostHolder.getUser().getId() == userId){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
    }
}
