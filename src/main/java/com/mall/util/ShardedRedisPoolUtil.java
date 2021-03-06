package com.mall.util;

import com.mall.common.ShardedRedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;

/**
 * Redis连接池工具，可以对Redis进行增删改查，还可以设置key的有效期
 *
 * @author yusiming
 * @date 2018/12/27 09:59
 */
public class ShardedRedisPoolUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisPoolUtil.class);

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     * @return 响应状态码
     */
    public static String set(String key, String value) {
        if (key == null || value == null) {
            return null;
        }
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = ShardedRedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            ShardedRedisPool.returnBrokenResource(jedis);
            LOGGER.error("set key: {} value: {} error", key, value, e);
        }
        ShardedRedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 获取key对应的value
     *
     * @param key 键
     * @return 对应的value
     */
    public static String get(String key) {
        // 如果key为空直接返回
        if (key == null) {
            return null;
        }
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = ShardedRedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            ShardedRedisPool.returnBrokenResource(jedis);
            LOGGER.error("get key: {} error", key, e);
        }
        ShardedRedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除key对应的value
     *
     * @param key 键
     * @return 返回删除的键的数量，如果该键不存在，返回0
     */
    public static Long del(String key) {
        if (key == null) {
            return null;
        }
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = ShardedRedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            ShardedRedisPool.returnBrokenResource(jedis);
            LOGGER.error("del key: {} error", key, e);
        }
        ShardedRedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置键值对的同时，设置过期时间
     * 默认是没有过期时间的
     *
     * @param key    键
     * @param value  值
     * @param exTime 有效期，单位是秒
     * @return 响应状态码
     */
    public static String setEx(String key, String value, int exTime) {
        if (key == null || value == null) {
            return null;
        }
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = ShardedRedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            ShardedRedisPool.returnBrokenResource(jedis);
            LOGGER.error("setEx key: {} value: {} error", key, value, e);
        }
        ShardedRedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置某一个键的过期时间
     *
     * @param key    键
     * @param exTime 过期时间
     * @return 返回1，表示设置成功，返回0表示设置失败（键不存在，或者redis版本小于2.1.3时，并且该键已经设置了过期时间）
     */
    public static Long expire(String key, int exTime) {
        if (key == null) {
            return null;
        }
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = ShardedRedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            ShardedRedisPool.returnBrokenResource(jedis);
            LOGGER.error("expire key: {}  error", key, e);
        }
        ShardedRedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        ShardedRedisPoolUtil.set("setKey", "setValue");
        String value = ShardedRedisPoolUtil.get("setKey");
        System.out.println(value);
        ShardedRedisPoolUtil.setEx("setExKey", "setExValue", 60 * 10);
        ShardedRedisPoolUtil.expire("setKey", 60 * 10);
        ShardedRedisPoolUtil.del("setExKey");
    }
}
