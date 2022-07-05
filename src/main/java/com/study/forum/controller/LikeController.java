package com.study.forum.controller;

import com.study.forum.service.LikeService;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import org.attoparser.ParsingCommentMarkupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Controller
public class LikeController {

    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityId, int entityType) {
        Integer userId = hostHolder.getUser().getId();
        likeService.like(entityType, entityId);
        long count = likeService.count(entityType, entityId);
        int islike = likeService.islike(userId, entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", count);
        map.put("likeStatus", islike);

        return CommunityUtil.getJSONString(0, null, map);

    }
}
