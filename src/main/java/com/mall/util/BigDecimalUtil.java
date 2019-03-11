package com.mall.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 使用BigDecimal一定要使用String构造器
 *
 * @author yusiming
 * @date 2018/11/29 20:02
 */
public class BigDecimalUtil {
    // 构造器私有化，只让使用静态方法
    private BigDecimalUtil() {
    }

    public static BigDecimal add(double v1, double v2) {
        return BigDecimal.valueOf(v1).add(BigDecimal.valueOf(v2));
    }

    public static BigDecimal sub(double v1, double v2) {
        return BigDecimal.valueOf(v1).subtract(BigDecimal.valueOf(v2));
    }

    public static BigDecimal mul(double v1, double v2) {
        return BigDecimal.valueOf(v1).multiply(BigDecimal.valueOf(v2));
    }

    public static BigDecimal div(double v1, double v2) {
        // 四舍五入
        return BigDecimal.valueOf(v1).divide(BigDecimal.valueOf(v2), RoundingMode.HALF_UP);
    }
}
