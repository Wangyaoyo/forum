package com.study.forum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // JDK普通线程池(初始化数量为5的线程池)
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;


    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExecutorService() {
        // 创建一个线程任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("------hello!testExecutorService------");
            }
        };

        // 重复执行10次
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        // 阻塞10秒
        sleep(10000);
    }

    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("------hello!testScheduledExecutorService------");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MICROSECONDS);
        sleep(20000);
    }

    // 3. spring线程池
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("------hello!testThreadPoolTaskExecutor------");
            }
        };

        // 重复执行10次
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(task);
        }

        // 阻塞10秒
        sleep(10000);
    }

    // 4. spring定时任务的线程池
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("------hello!ThreadPoolTaskScheduler------");
            }
        };

        // 延迟一万毫秒的时间
        Date startTime = new Date(System.currentTimeMillis() + 10000);
        // 配置定时任务的开始时间和任务间隔执行时间
        threadPoolTaskScheduler.scheduleAtFixedRate(task, startTime, 1000);

        // 阻塞10秒
        sleep(30000);
    }
}
