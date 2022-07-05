package com.study.forum.service;

import com.study.forum.util.HostHolder;
import com.study.forum.util.RedisKeyUtil;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HostHolder hostHolder;

    // 当前登录用户为id为entityId、类型为entityType的实体点赞
    public void like(int entityType, int entityId) {
        Integer userId = hostHolder.getUser().getId();
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
        if (member) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }
    }

    // 统计某实体的赞的数量
    public long count(int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    // 查看userId是否为id为entityId、类型为entityType的实体点赞
    public int islike(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
        return member ? 1 : 0;
    }
}
