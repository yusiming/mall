package com.mall.common;

import com.mall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;

import java.util.ArrayList;

/**
 * 分布式redis连接池
 *
 * @author yusiming
 * @date 2019/3/6 16:49
 */
public class ShardedRedisPool {
    // 分片的jedis连接池
    private static ShardedJedisPool pool;
    // 最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 在jedisPool中idle（空闲）状态的最大的jedis实例的个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    // 在jedisPool中idle（空闲）状态的最小的jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    /**
     * 在borrow一个jedis实例的时候，是否要进行验证操作，
     * 如果此属性设置为true，则从连接池中得到的连接都是可用的，该属性默认为false
     */
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    /**
     * 在return一个jedis实例的时候，是否要进行验证操作，
     * 如果此属性设置为true，则返回的连接都是可用的,该属性的默认值为false
     * 该属性设置为false时，通过一些代码的逻辑可以提高连接效率
     */
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return",
            "false"));
    // redis1服务器的ip和端口
    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip", "192.168.0.200");
    private static int redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379"));
    private static String redis1password = PropertiesUtil.getProperty("redis1.password");

    // redis2服务器的ip和端口
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "192.168.0.200");
    private static int redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6379"));
    private static String redis2password = PropertiesUtil.getProperty("redis2.password");

    /**
     * 初始化连接池
     * 注意：这个方法是private的，避免被外部调用
     */
    private static void initPool() {
        // 初始化配置
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置关键属性
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        // 连接耗尽时阻塞直到超时，如果超时则抛出超时异常，如果设置为false，则连接耗尽时抛出异常，该属性默认值为true
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 2000);
        info1.setPassword(redis1password);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 2000);
        info2.setPassword(redis2password);
        ArrayList<JedisShardInfo> list = new ArrayList<>();
        list.add(info1);
        list.add(info2);

        pool = new ShardedJedisPool(config, list, Hashing.MURMUR_HASH);
    }

    static {
        // 在类被加载时就调用初始化方法
        initPool();
    }

    /**
     * 从连接池中获取Jedis实例（连接）
     *
     * @return Jedis实例
     */
    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    /**
     * 返回Jedis实例（连接）
     *
     * @param jedis 实例
     */
    public static void returnResource(ShardedJedis jedis) {
        // 这里不需要判断是否为空，pool的returnResource方法已经进行了判断
        pool.returnResource(jedis);
    }

    /**
     * 返回损坏的Jedis实例
     *
     * @param jedis 实例
     */
    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();
        jedis.set("testKey", "testValue");
        returnResource(jedis);
        // 测试时临时调用，销毁连接池中的所有链接
        pool.destroy();
        System.out.println("test end!");
    }

}
