package com.study.forum.controller;

import com.study.forum.event.EventProducer;
import com.study.forum.pojo.Comment;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.pojo.Event;
import com.study.forum.service.CommentService;
import com.study.forum.service.DiscussPostService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.HostHolder;
import com.study.forum.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author wy
 * @version 1.0
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 增加评论
     * @param id
     * @param comment
     * @return
     */
    @RequestMapping(value = "/add/{postId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("postId") int id, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        if(comment.getTargetId() == null){
            comment.setTargetId(0);
        }

        int add = commentService.add(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setData("postId", id);

        // 如果评论对象是帖子查询帖子表，如果是评论查询评论表
        if(comment.getEntityId() == ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.getById(id);
            event.setEntityUserId(post.getUserId());
        }else if(comment.getEntityId() == ENTITY_TYPE_COMMENT){
            Comment com = commentService.getById(comment.getEntityId());
            event.setEntityUserId(com.getUserId());
        }
        // 发布一个事件
        eventProducer.sendEvent(event);

        if(comment.getEntityId() == ENTITY_TYPE_POST){
            // 如果发布的是帖子不是评论，触发添加帖子的事件
            Event e = new Event();
            e.setUserId(hostHolder.getUser().getId())
                    .setTopic(TOPIC_PUBLISH)
                    .setEntityId(id)
                    .setEntityType(ENTITY_TYPE_POST);
            eventProducer.sendEvent(e);

            // 如果对帖子进行评论，将 postId 加入缓存待计算分数
            String postKey = RedisKeyUtil.getPostKey();
            redisTemplate.opsForSet().add(postKey, id);
        }

        return "redirect:/postdetail/" + id;
    }
}
