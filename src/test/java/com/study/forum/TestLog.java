package com.study.forum;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;



/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
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
