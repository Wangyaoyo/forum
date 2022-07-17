package com.study.forum.service;

import com.study.forum.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    /**
     * 将指定的IP记入UV
     *
     * @param ip
     */
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(sdf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
        logger.info("ip为{}的用户记录为UV", ip);
    }

    /**
     * 统计指定范围日期内的UV
     *
     * @param start
     * @param end
     * @return
     */
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 整理该日期范围内的key, 为合并数据做准备
        ArrayList<String> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisKeyUtil.getUVKey(sdf.format(calendar.getTime()));
            keys.add(uvKey);
            // 加一天
            calendar.add(Calendar.DATE, 1);
        }
        // 将这些数据合并
        String unionKey = RedisKeyUtil.getUVKey(sdf.format(start), sdf.format(end));
        redisTemplate.opsForHyperLogLog().union(unionKey, keys.toArray());
        logger.info("统计{}到{}范围内的UV", start, end);
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }

    /**
     * 将指定的用户记入DAU
     *
     * @param userId
     */
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(sdf.format(new Date()));
        // 以daukey为键 在第 userId个位置存入 true
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
        logger.info("将Id为{}的用户记录进DAU", userId);
    }

    /**
     * 统计指定范围内的DAU数量
     *
     * @param start
     * @param end
     * @return
     */
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 整理该日期范围内的key, 为合并数据做准备
        ArrayList<byte[]> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String dauKey = RedisKeyUtil.getDAUKey(sdf.format(calendar.getTime()));
            // 将 String 转化成 byte数组
            keys.add(dauKey.getBytes());
            // 加一天
            calendar.add(Calendar.DATE, 1);
        }

        logger.info("统计{}到{}范围内的DAU", start, end);
        // 进行or运算： 是指将多个 byte 数组中的 1 的个数相加
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisKeyUtil.getDAUKey(sdf.format(start), sdf.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(), keys.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        });
    }
}
