package com.study.forum.controller;

import com.study.forum.service.FollowService;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        Integer userId = hostHolder.getUser().getId();
        followService.follow(userId, entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    /**
     * 某人（当前用户）取消对某实体的关注
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
}
