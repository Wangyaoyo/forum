package com.study.forum.util;

import com.study.forum.pojo.User;
import org.springframework.stereotype.Component;

/**
 * 将user放入ThreadLocal中，此类封装了ThreadLocal
 * @author wy
 * @version 1.0
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
