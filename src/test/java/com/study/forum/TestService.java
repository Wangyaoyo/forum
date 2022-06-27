package com.study.forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.pojo.Comment;
import com.study.forum.service.CommentService;
import com.study.forum.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
public class TestService {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void test01() {
        Page<Comment> page = discussPostService.getPageComment(1, 233, 1, 10);
        System.out.println(page.getRecords().toString());
    }


}
