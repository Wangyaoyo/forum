package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.CommentMapper;
import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.Comment;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;


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

    /**
     * 分页：用户帖子
     *
     * @param userId
     * @param offset：从第几页开始
     * @param limit：条数
     * @return
     */
    public Page<DiscussPost> getPageDiscussPosts(int userId, Integer offset, int limit) {
        if (offset == null)
            offset = 0;
        Page<DiscussPost> page = new Page<>(offset, limit);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        if (userId != 0) {
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("type");
        queryWrapper.orderByDesc("create_time");
        discussPostMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 得到某用户的帖子条数
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
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("type", type).eq("id", id);
        int update = discussPostMapper.update(null, wrapper);
        if(update == 1)
            logger.info("帖子id为{}的帖子修改类型为：{}", id, type);
        return update;
    }

    /**
     * 修改帖子状态： 正常/精华/拉黑
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status) {
        UpdateWrapper<DiscussPost> wrapper = new UpdateWrapper<>();
        wrapper.set("status", status).eq("id", id);
        int update = discussPostMapper.update(null, wrapper);
        if(update == 1)
            logger.info("帖子id为{}的帖子修改状态为：{}", id, status);
        return update;
    }

}
