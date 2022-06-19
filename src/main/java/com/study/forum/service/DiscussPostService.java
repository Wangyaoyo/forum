package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 分页：用户帖子
     * @param userId
     * @param offset：从第几页开始
     * @param limit：条数
     * @return
     */
    public  Page<DiscussPost> getPageDiscussPosts(int userId, Integer offset, int limit){
        if(offset == null)
            offset = 0;
        Page<DiscussPost> page = new Page<>(offset, limit);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        if(userId != 0){
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("type");
        queryWrapper.orderByDesc("create_time");
        discussPostMapper.selectPage(page, queryWrapper);
        return page;
    }

    /**
     * 得到某用户的帖子条数
     * @param userId
     * @return
     */
    public int getDiscussPostRows(int userId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("user_id", userId);
        Integer count = discussPostMapper.selectCount(queryWrapper);
        return count;
    }
}
