package com.mall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @Auther yusiming
 * @Date 2018/11/29 19:52
 */
public class BigDecimalTest {
    @Test
    public void test1() {
        // 商业运算中，丢失精度的问题是一定要注意的
        System.out.println(0.05 + 0.01); // 0.060000000000000005
        System.out.println(1.0 - 0.42); // 0.5800000000000001
        System.out.println(4.015 * 100); // 401.49999999999994
        System.out.println(123.3 / 100); // 1.2329999999999999
    }

    @Test
    public void test2() {
        // 商业运算中，丢失精度的问题是一定要注意的
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        // 0.06000000000000000298372437868010820238851010799407958984375
        System.out.println(b1.add(b2));
    }

    @Test
    public void test3() {
        // 商业运算中，丢失精度的问题是一定要注意的
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        // 0.06
        System.out.println(b1.add(b2));
    }

    @Test
    public void test4() {
        // 0.06
        System.out.println(BigDecimal.valueOf(0.05).add(BigDecimal.valueOf(0.01)));
        //     public static BigDecimal valueOf(double val) {
        //         // Reminder: a zero double returns '0.0', so we cannot fastpath
        //         // to use the constant ZERO.  This might be important enough to
        //         // justify a factory approach, a cache, or a few private
        //         // constants, later.
        //         return new BigDecimal(Double.toString(val));
        //     }
    }

}
