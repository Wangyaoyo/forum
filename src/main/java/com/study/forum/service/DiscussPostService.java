package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.study.forum.mapper.CommentMapper;
import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.Comment;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author wy
 * @version 1.0
 */
@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    /* caffeine 存储的最大数量 */
    @Value("${spring.cache.caffeine.posts.max-size}")
    private Integer maxSize;

    /* caffeine 过期时间 */
    @Value("${spring.cache.caffeine.posts.expire-seconds}")
    private Integer expireSeconds;

    /* caffeine 初始化 */
    @PostConstruct
    public void init() {
        /* 初始化列表缓存 */
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        // 键值为空
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        // 键值不为空: 分割为 offset + limit
                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        /* 此处可以设置二级缓存 e.g. redis缓存 */

                        // 查询数据库初始化 postList
                        logger.info("Init postlist from DB.");
                        Page<DiscussPost> page = new Page<>(offset, limit);
                        QueryWrapper queryWrapper = new QueryWrapper();
                        queryWrapper.ne("status", 2);
                        queryWrapper.orderByDesc("type");
                        queryWrapper.orderByDesc("score");
                        queryWrapper.orderByDesc("create_time");
                        return discussPostMapper.selectPage(page, queryWrapper).getRecords();
                    }
                });
    }

    /* caffeine： postList缓存 */
    private LoadingCache<String, List<DiscussPost>> postListCache;


    /**
     * 分页查询帖子 ： 走 DB/缓存
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    public Map<String, Object> getPageDiscussPosts(int userId, Integer offset, int limit, int orderMode) {
        if (offset == null)
            offset = 0;
        Map<String, Object> res = new HashMap<>();
        /* 查询首页的最热帖 首先使用一级缓存 */
        if(userId == 0 && orderMode == 1){
            logger.info("Query postlist from Caffeine.");
            List<DiscussPost> posts = postListCache.get(offset + ":" + limit);
            res.put("posts", posts);
            return res;
        }

        logger.info("Query postlist from DB.");
        Page<DiscussPost> page = new Page<>(offset, limit);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        // 传入userId的话 即 查询某人的帖子
        if (userId != 0) {
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("type");
        /* 按照帖子分数的倒序排列 : 1代表最热 */
        if (orderMode == 1) {
            queryWrapper.orderByDesc("score");
        }
        queryWrapper.orderByDesc("create_time");
        discussPostMapper.selectPage(page, queryWrapper);
        res.put("page", page);
        res.put("posts", page.getRecords());
        return res;
    }

    /**
     * 得到某用户的帖子条数 ： 走 DB/缓存
     *
     * @param userId
     * @return
     */
    public int getDiscussPostRows(int userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("user_id", userId);
        Integer count = discussPostMapper.selectCount(queryWrapper);
        return count;
    }

    public int insert(DiscussPost discussPost) {
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insert(discussPost);
    }


    public DiscussPost getById(int id) {
        return discussPostMapper.selectById(id);
    }

    public Page<Comment> getPageComment(int entityType, int entityId, Integer current, int limit) {
        if (current == null)
            current = 0;
        Page<Comment> commentPage = new Page<>(current, limit);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        // 帖子 or 评论
        wrapper.eq("entity_type", entityType);
        // 帖子id or 评论id
        wrapper.eq("entity_id", entityId);
        wrapper.orderByDesc("create_time");
        commentMapper.selectPage(commentPage, wrapper);
        return commentPage;
    }

    public int updateCommentCount(int postId, int count) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("comment_count", count).eq("id", postId);
        return discussPostMapper.update(null, wrapper);
    }

    /**
     * 修改帖子状态：普通/置顶
     *
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("type", type).eq("id", id);
        int update = discussPostMapper.update(null, wrapper);
        if (update == 1)
            logger.info("帖子id为{}的帖子修改类型为：{}", id, type);
        return update;
    }

    /**
     * 修改帖子状态： 正常/精华/拉黑
     *
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("status", status).eq("id", id);
        int update = discussPostMapper.update(null, wrapper);
        if (update == 1)
            logger.info("帖子id为{}的帖子修改状态为：{}", id, status);
        return update;
    }

    /**
     * 更新帖子分数
     *
     * @param postId
     * @param score
     */
    public int updateScore(int postId, double score) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("score", score).eq("id", postId);
        return discussPostMapper.update(null, wrapper);
    }

}
