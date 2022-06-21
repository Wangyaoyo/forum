package com.study.forum.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author wy
 * @version 1.0
 */
public class CookieUtil {
    /**
     * 通过键得到相应的cookie值
     */
    public static String getValue(HttpServletRequest request, String cookieName) {
        if(request == null || cookieName == null){
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
