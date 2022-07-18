package com.study.forum.controller;

import com.study.forum.event.EventProducer;
import com.study.forum.pojo.Event;
import com.study.forum.service.LikeService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import com.study.forum.util.RedisKeyUtil;
import org.attoparser.ParsingCommentMarkupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
public class LikeController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityId, int entityType, int entityUserId, int postId) {
        Integer userId = hostHolder.getUser().getId();
        likeService.like(entityType, entityId, entityUserId);
        long count = likeService.count(entityType, entityId);
        int islike = likeService.islike(userId, entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", count);
        map.put("likeStatus", islike);

        if(islike == 1){
            // 触发一个Event事件
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(userId)
                    .setEntityId(entityId)
                    .setEntityType(entityType)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            // 发布一个事件
            eventProducer.sendEvent(event);
        }

        // 如果对帖子点赞才计算帖子分数, 对评论以及别的点赞不计算分数
        if(entityType == ENTITY_TYPE_POST){
            // 如果对帖子进行评论，将 postId 加入缓存待计算分数
            String postKey = RedisKeyUtil.getPostKey();
            redisTemplate.opsForSet().add(postKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);

    }
}
