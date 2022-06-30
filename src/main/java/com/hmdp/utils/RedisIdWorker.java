package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Redis生成全局唯一ID的方案
 * 1个 符号位 + 31位 时间戳  + 32位 序列号位
 */
@Component
public class RedisIdWorker {
    /**
     * 开始时间戳 取的2022-01-01 00：00：00时刻
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 序列号的位数
     */
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1.获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.自增长，redis的自增上线是2的64次方， 为了使得不会报越界， 在key的后面附加了一个日期。所以同一个key的数据是同一天的， 同一天数量超过的2的64次方的可能性就很小很小了
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.拼接全局ID的三部分(1个 符号位 + 31位 时间戳  + 32位 序列号位)并返回
        return timestamp << COUNT_BITS | count;
    }

//    public static void main(String[] args) {
//        LocalDateTime time = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
//        long l = time.toEpochSecond(ZoneOffset.UTC);
//        System.out.println(l);
//    }
}
