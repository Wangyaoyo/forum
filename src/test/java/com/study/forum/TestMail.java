package com.study.forum;

import com.study.forum.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
public class TestMail {

    @Autowired
    MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("1368829476@qq.com", "测试","欢迎！");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "Wy");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("1368829476@qq.com", "HTML", content);
    }
}
