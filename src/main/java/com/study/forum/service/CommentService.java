package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.study.forum.mapper.CommentMapper;
import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.Comment;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int add(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数为空异常！");
        }
        // 过滤评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int row = commentMapper.insert(comment);
        // 如果是对帖子的评论需要更新评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("entity_type", comment.getEntityType());
            wrapper.eq("entity_id", comment.getEntityId());
            int count = commentMapper.selectCount(wrapper);
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return row;
    }

    public Comment getById(int id) {
        return commentMapper.selectById(id);
    }
}
