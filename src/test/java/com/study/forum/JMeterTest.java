package com.study.forum;

import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.pojo.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JMeterTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void insertPost() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setTitle("互联网求职A计划");
            discussPost.setContent("2023有更好的就业形势！！！！！！！！！！");
            discussPost.setUserId(111);
            discussPost.setCreateTime(new Date());
            discussPost.setScore(0.0);
            discussPost.setStatus(0);
            discussPost.setType(0);
            discussPost.setCommentCount(0);
            discussPostMapper.insert(discussPost);
        }
    }
}
