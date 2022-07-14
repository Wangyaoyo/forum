package com.study.forum;

import com.study.forum.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSensitive {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testsesitive(){
        String filter = sensitiveFilter.filter("这里可以吸毒，了快递费嫖娼，可以赌博");
        System.out.println(filter);
    }
}
