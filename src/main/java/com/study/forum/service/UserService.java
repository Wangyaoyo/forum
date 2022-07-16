package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.study.forum.mapper.UserMapper;
import com.study.forum.pojo.LoginTicket;
import com.study.forum.pojo.User;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.MailClient;
import com.study.forum.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.recycler.Recycler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int userId) {
        User user = getUserCache(userId);
        if (user == null) {
            user = initCache(userId);
        }
        return user;
    }

    /*  从缓存中获取数据  */
    private User getUserCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /*  缓存中没有从数据库查询并设置缓存  */
    private User initCache(int userId) {
        // 从数据库中查询
        QueryWrapper<User> userwrapper = new QueryWrapper<>();
        userwrapper.eq("id", userId);
        User user = userMapper.selectOne(userwrapper);
        // 设置到缓存
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user);

        return user;
    }

    /* 数据变更时清除缓存数据  */
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
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
            clearCache(user.getId());       // 数据修改，清除redis缓存
            return CommunityConstant.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        // 验证用户名密码为空、用户是否存在、账号是否激活、密码是否正确、
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码为空！");
            return map;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insert(loginTicket);

        // 使用redis存储ticket
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        /* 划重点：很好用
        UpdateWrapper<LoginTicket> wrapper = new UpdateWrapper<>();
        wrapper.set("status", 1).eq("ticket", ticket);
        loginTicketMapper.update(null, wrapper);
         */
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        // 修改状态后重新存入
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket getLoginTicketByTicket(String ticket) {
        /*
        QueryWrapper<LoginTicket> loginwrapper = new QueryWrapper<>();
        loginwrapper.eq("ticket", ticket);
        LoginTicket loginTicket = loginTicketMapper.selectOne(loginwrapper);
         */
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int id, String url) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("header_url", url).eq("id", id);
        int rows = userMapper.update(null, wrapper);
        clearCache(id);
        return rows;
    }

    public int changePass(int id, String password) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("password", password).eq("id", id);
        int rows = userMapper.update(null, wrapper);
        clearCache(id);
        return rows;
    }

    public User findUserByName(String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", name);
        return userMapper.selectOne(wrapper);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
