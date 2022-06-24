package com.study.forum;

import com.study.forum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
public class TestSensitive {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testsesitive(){
        String filter = sensitiveFilter.filter("这里可以吸毒，了快递费嫖娼，可以赌博");
        System.out.println(filter);
    }
}
