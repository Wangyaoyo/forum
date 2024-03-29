package com.study.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.annotation.LoginRequired;
import com.study.forum.event.EventProducer;
import com.study.forum.pojo.Comment;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.pojo.Event;
import com.study.forum.pojo.User;
import com.study.forum.service.DiscussPostService;
import com.study.forum.service.LikeService;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import com.study.forum.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author wy
 * @version 1.0
 */
@Controller
public class DiscussPostController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

//    @GetMapping("/discussposts/{userId}")
//    public int getRowNumbers(@PathVariable("userId") int userId) {
//        return discussPostService.getDiscussPostRows(userId);
//    }

    @RequestMapping("/myposts")
    public String getMyPosts(Model model,
                             @RequestParam(value = "current", required = false) Integer current) {
        Integer userId = hostHolder.getUser().getId();
        logger.info("查询我的帖子：From DB");
        Page<DiscussPost> page = (Page<DiscussPost>) discussPostService.getPageDiscussPosts(userId, current, 5, 0).get("page");
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (page != null) {
            for (DiscussPost post : page.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("post", post);
                long count = likeService.count(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", count);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("page", page);
        return "/site/my-post";
    }

    @LoginRequired
    @RequestMapping(value = "/publishpost", method = RequestMethod.POST)
    @ResponseBody
    public String publishPost(String title, String content) {
        // 判空
        User user = hostHolder.getUser();
        if (user == null) {
            logger.info("用户还未登录！");
            return CommunityUtil.getJSONString(403, "您还没有登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPost.setScore(0.0);
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCommentCount(0);
        discussPostService.insert(discussPost);

        // 构造Event对象，异步存入ES服务器
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.sendEvent(event);

        // 将帖子放入redis中待计算分数
        String postKey = RedisKeyUtil.getPostKey();
        // 防止出现重复数据，且对顺序无要求: 选用 set 集合
        redisTemplate.opsForSet().add(postKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @LoginRequired
    @RequestMapping(value = "/postdetail/{id}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable(value = "id", required = false) int id,
                                 @RequestParam(value = "current", required = false) Integer current,
                                 Model model) {
        DiscussPost post = discussPostService.getById(id);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("post", post);
        // DONE: 查询帖子回复
        Page<Comment> pageComment = discussPostService.getPageComment(CommunityConstant.ENTITY_TYPE_POST, id, current, 10);
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (pageComment.getSize() > 0) {
            // 循环每条评论
            for (Comment comment : pageComment.getRecords()) {
                Map<String, Object> commentVo = new HashMap<>();
                // 该评论本身
                commentVo.put("comment", comment);
                // 该评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 得到评论的评论
                Page<Comment> replyPage = discussPostService.getPageComment(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId(), 0, 10);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyPage.getSize() > 0) {
                    for (Comment reply : replyPage.getRecords()) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        replyVo.put("target", userService.findUserById(reply.getTargetId()));
                        long replyCount = likeService.count(ENTITY_TYPE_COMMENT, reply.getEntityId());
                        int replyislike = likeService.islike(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getEntityId());
                        replyVo.put("likeCount", replyCount);
                        replyVo.put("likeStatus", replyislike);
                        replyVoList.add(replyVo);
                    }
                }
                long replyCount = likeService.count(ENTITY_TYPE_COMMENT, comment.getId());
                int replyislike = likeService.islike(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", replyCount);
                commentVo.put("likeStatus", replyislike);
                commentVo.put("replys", replyVoList);
                commentVo.put("replyCount", replyPage.getTotal());
                commentVoList.add(commentVo);
            }
        }
        long count = likeService.count(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", count);
        int islike = likeService.islike(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", islike);

        model.addAttribute("comments", commentVoList);
        model.addAttribute("commentCount", pageComment.getTotal());
        model.addAttribute("page", pageComment);
        return "/site/discuss-detail";
    }

    /**
     * 置顶帖子
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateType(id, POST_TYPE_TOP);

        // 更新状态到es
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);

        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精帖子
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateStatus(id, POST_STATUS_GOOD);

        // 更新状态到es
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(TOPIC_PUBLISH)
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.sendEvent(event);

        // 将帖子放入redis中待计算分数
        String postKey = RedisKeyUtil.getPostKey();
        // 防止出现重复数据，且对顺序无要求: 选用 set 集合
        redisTemplate.opsForSet().add(postKey, id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除帖子(拉黑)
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateStatus(id, POST_STATUS_BLACK);

        // 触发删帖事件
        Event event = new Event()
                .setUserId(user.getId())
                .setTopic(TOPIC_DELETE)
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);

        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0);
    }

}
