package com.mall.common;

/**
 * 常量类
 *
 * @Auther yusiming
 * @Date 2018/11/22 22:37
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface Role {
        //普通用户
        int ROLE_CUSTOMER = 0;
        // 管理员
        int ROLE_ADMIN = 1;
    }
}
