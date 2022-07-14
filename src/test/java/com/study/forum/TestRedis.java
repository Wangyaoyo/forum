package com.study.forum;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wy
 * @version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test01() {

//        String valuekey = "like:entity:1:275";
//        redisTemplate.opsForValue();
//        System.out.println(redisTemplate.opsForValue().get(valuekey));
        String key = "like:entity:2:163";
        Boolean member = redisTemplate.opsForSet().isMember(key, 111);
        System.out.println(member);


    }
}
