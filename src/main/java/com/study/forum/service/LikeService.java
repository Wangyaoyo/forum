package com.study.forum.service;

import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import com.study.forum.util.RedisKeyUtil;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
    // 同时记录被点赞的帖子/评论的作者被点赞的数量（使用事务）
    public void like(int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                Integer userId = hostHolder.getUser().getId();
                String entityLikekey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean member = redisTemplate.opsForSet().isMember(entityLikekey, userId);

                operations.multi();

                if (member) {
                    redisTemplate.opsForSet().remove(entityLikekey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    redisTemplate.opsForSet().add(entityLikekey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
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

    // 返回某用户的被点赞数量
    public int findUserLikeCount(int userId) {
        String userKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userKey);
        return count == null ? 0 : count.intValue();
    }
}
