package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.forum.mapper.UserMapper;
import com.study.forum.pojo.User;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int userId) {
        User user = userMapper.selectById(userId);
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 判空
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        // 用户名及邮箱 是否注册过
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User result1 = userMapper.selectOne(wrapper);
        if (result1 != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        wrapper.eq("email", user.getEmail());
        User result2 = userMapper.selectOne(wrapper);
        if (result2 != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 补充剩余用户信息，并注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insert(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/activation/101/code
        // http://localhost:8080/activation/0/447f790952e44a998dbeeb87d57a8193
        // 获取新id
        wrapper.eq("username", user.getUsername());
        User newUser = userMapper.selectOne(wrapper);
        context.setVariable("url", domain + "activation/" + newUser.getId() + "/" + user.getActivationCode());
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activate(Integer id, String code) {
        User user = userMapper.selectById(id);
        if (user.getStatus() == 1) {
            return CommunityConstant.ACTIVATION_REPEATE;
        } else if (code.equals(user.getActivationCode())) {
            user.setStatus(1);
            userMapper.updateById(user);
            return CommunityConstant.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }
}
