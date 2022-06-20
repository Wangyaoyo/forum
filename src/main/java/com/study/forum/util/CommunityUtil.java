package com.study.forum.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 *
 * @author wy
 * @version 1.0
 */
public class CommunityUtil {

    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密
     * @param key
     * @return
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
