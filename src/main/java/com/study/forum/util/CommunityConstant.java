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

}
