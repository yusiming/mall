package com.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author yusiming
 * @date 2018/11/23 10:52
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    public static String TOKEN_PREFIX = "token_";
    /**
     * 创建一个本地缓存，初始化容量1000，最大容量10000，
     * 如果超过10000，将使用LRU算法，最少使用算法进行清除
     * 缓存的有效期12小时
     */
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000)
            .maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
                // 默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法
                @Override
                public String load(String key) throws Exception {
                    // 即是没有找到对应的value，也返回一个字符串"null"，而不是一个null
                    return "null";
                }
            });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("local cache get error", e);
        }
        return null;
    }

    public static void removeKey(String key) {
        if (StringUtils.isNotBlank(key)) {
            localCache.invalidate(key);
        }
    }

    public static void main(String[] args) throws ExecutionException {
        LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000)
                .maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
                    // 默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法
                    @Override
                    public String load(String key) throws Exception {
                        // 即是没有找到对应的value，也返回一个字符串"null"，而不是一个null
                        return "null";
                    }
                });
        localCache.put("username", "虞四明");
        String username = localCache.get("username");
        System.out.println(username);
        localCache.invalidate("username");
        username = localCache.get("username");
        System.out.println(username);
    }
}
