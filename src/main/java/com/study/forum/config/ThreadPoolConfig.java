package com.study.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wy
 * @version 1.0
 */
@Configuration
// 启动定时任务
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
