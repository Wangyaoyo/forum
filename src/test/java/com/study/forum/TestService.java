package com.study.forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.MessageMapper;
import com.study.forum.pojo.Comment;
import com.study.forum.pojo.Message;
import com.study.forum.pojo.User;
import com.study.forum.service.CommentService;
import com.study.forum.service.DiscussPostService;
import com.study.forum.service.MessageService;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
public class TestService {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserService userService;

    @Test
    public void test01() {
        Page<Comment> page = discussPostService.getPageComment(1, 233, 1, 10);
        System.out.println(page.getRecords().toString());
    }

    @Test
    public void test02() {
        Page<Message> messagesList = messageMapper.getMessagesList(new Page<Message>(1,10), 111);
        System.out.println(messagesList.getTotal());
        System.out.println(messagesList.getSize());
        System.out.println(messagesList.getRecords());
    }

    @Test
    public void test03() {
        userService.changePass(111, CommunityUtil.md5("1234"+"167f9"));
    }
}
