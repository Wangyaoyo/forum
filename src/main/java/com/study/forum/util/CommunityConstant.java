package com.study.forum.util;

import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * @author wy
 * @version 1.0
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 激活重复
     */
    int ACTIVATION_REPEATE = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：发布帖子
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 系统通知
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 消息未读
     */
    int MESSAGE_UNREAD = 0;

    /**
     * 消息已读
     */
    int MESSAGE_HAS_READ = 1;

    /**
     * 消息未删除
     */
    int MESSAGE_UNDELETED = 2;

    /**
     * 每页最大数据条数
     */
    int PAGE_LIMIT = 10;

    /**
     * 用户权限
     */
    String AUTHORITY_USER = "user";

    /**
     * 管理员权限
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 版主权限
     */
    String AUTHORITY_MODERATOR = "moderator";
}
