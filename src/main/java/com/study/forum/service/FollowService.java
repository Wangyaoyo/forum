package com.study.forum.service;

import com.study.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 增加一个关注（帖子、评论、用户）  entityType、entityId
    // 增加一个粉丝（用户）  userId
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 某实体多了一个粉丝用户
                // 某用户多了一个关注实体
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // redis事务的开始
                redisTemplate.multi();
                // userId关注的实体
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // 当前实体被userId取消关注
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                // redis事务的结束
                redisTemplate.exec();

                return null;
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 某实体多了一个粉丝用户
                // 某用户多了一个关注实体
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // redis事务的开始
                redisTemplate.multi();
                // userId关注的实体
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                //userId关注了当前用户？？？
                redisTemplate.opsForZSet().remove(followerKey, userId);
                // redis事务的结束
                redisTemplate.exec();

                return null;
            }
        });
    }

    // 查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }
}
