package com.study.forum.controller;

import com.study.forum.util.CommunityUtil;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author wy
 * @version 1.0
 */
@RequestMapping("/cands")
@RestController
public class DemoController {

    /**
     * 服务端生成cookie
     * @param response
     * @return
     */
    @RequestMapping("/cookie/set")
    public String get(HttpServletResponse response){
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置携带cookie的请求路径
        cookie.setPath("/cookie/alpha");
        cookie.setMaxAge(600);
        response.addCookie(cookie);
        return "setCookie";
    }

    /**
     * 客户端携带cookie的请求
     * @param code
     * @return
     */
    @RequestMapping("/cookie/alpha")
    public String withcookie(@CookieValue("code") String code){
        System.out.println(code);
        return code;
    }

    @RequestMapping("/session/set")
    public String setsession(HttpSession session){
        session.setAttribute("id", "123213243");
        session.setAttribute("name", "wy");
        return "set session";
    }

    @RequestMapping("/session/get")
    public String getsession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get";
    }
}
