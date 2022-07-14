package com.study.forum;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.pojo.User;
import com.study.forum.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class ForumApplicationTests {
    private static final Logger logger = LoggerFactory.getLogger(ForumApplicationTests.class);


    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    private UserService userService;

    @Test
    void discussPost() {
        Page<DiscussPost> page = new Page<>(1,2);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("user_id", 111);
        queryWrapper.orderByDesc("type");
        queryWrapper.orderByDesc("create_time");
        discussPostMapper.selectPage(page, queryWrapper);
        System.out.println(page.getRecords());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("当前页："+page.getCurrent());

    }


    @Test
    void discussPostCount() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("user_id", 111);
        queryWrapper.orderByDesc("type");
        queryWrapper.orderByDesc("create_time");
        Integer count = discussPostMapper.selectCount(queryWrapper);
        System.out.println(count);
    }

    @Test
    void user() {
        User userById = userService.findUserById(103);
        System.out.println(userById);
    }

    @Test
    public void test01(){
        System.out.println(logger.getName());
        logger.info("info");
        logger.debug("debug");
        logger.error("error");
        logger.warn("warn");
    }
}
