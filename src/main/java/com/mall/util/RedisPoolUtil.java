package com.mall.util;

import com.mall.common.RedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Redis连接池工具，可以对Redis进行增删改查，还可以设置key的有效期
 *
 * @author yusiming
 * @date 2018/12/27 09:59
 */
public class RedisPoolUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPoolUtil.class);

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     * @return 响应状态码
     */
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            RedisPool.returnBrokenResource(jedis);
            LOGGER.error("set key: {} value: {} error", key, value, e);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 获取key对应的value
     *
     * @param key 键
     * @return 对应的value
     */
    public static String get(String key) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            RedisPool.returnBrokenResource(jedis);
            LOGGER.error("get key: {} error", key, e);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除key对应的value
     *
     * @param key 键
     * @return 返回删除的键的数量，如果该键不存在，返回0
     */
    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            RedisPool.returnBrokenResource(jedis);
            LOGGER.error("del key: {} error", key, e);
        }
        RedisPool.returnResource(jedis);
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
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            RedisPool.returnBrokenResource(jedis);
            LOGGER.error("setEx key: {} value: {} error", key, value, e);
        }
        RedisPool.returnResource(jedis);
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
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            RedisPool.returnBrokenResource(jedis);
            LOGGER.error("expire key: {}  error", key, e);
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        RedisPoolUtil.set("setKey", "setValue");
        String value = RedisPoolUtil.get("setKey");
        System.out.println(value);
        RedisPoolUtil.setEx("setExKey", "setExValue", 60 * 10);
        RedisPoolUtil.expire("setKey", 60 * 10);
        RedisPoolUtil.del("setExKey");
    }
}
