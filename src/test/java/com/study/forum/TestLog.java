package com.study.forum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestLog {
    private static final Logger logger = LoggerFactory.getLogger(TestLog.class);

    @Test
    public void test01(){
        System.out.println(logger.getName());
        logger.info("info");
        logger.debug("debug");
        logger.error("error");
        logger.warn("warn");
    }
}
