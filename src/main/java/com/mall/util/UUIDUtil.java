package com.mall.util;

import java.util.UUID;

/**
 * UUID工具类
 *
 * @author yusiming
 * @date 2019/3/6 12:09
 */
public class UUIDUtil {
    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
