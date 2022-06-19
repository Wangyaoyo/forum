package com.study.forum.service;

import com.study.forum.mapper.UserMapper;
import com.study.forum.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int userId){
        User user = userMapper.selectById(userId);
        return user;
    }
}
