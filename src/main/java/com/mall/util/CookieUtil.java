package com.mall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie的工具类
 *
 * @author yusiming
 * @date 2019/3/5 21:53
 */
public class CookieUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieUtil.class);
    private static String COOKIE_NAME = "login_token";
    private static String COOKIE_DOMAIN = ".mall.com";

    /**
     * 向客户端设置一个用来保存登陆令牌的cookie
     *
     * @param response 响应
     * @param token    令牌
     */
    public static void sendLoginCookie(HttpServletResponse response, String token) {
        // cookie的name属性
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        // cookie的domain属性
        cookie.setDomain(COOKIE_DOMAIN);
        // cookie的path
        cookie.setPath("/");
        // cookie的过期时间为一个月
        cookie.setMaxAge(60 * 60 * 24 * 30);
        LOGGER.info("send cookieName:{} value:", COOKIE_NAME, token);
        response.addCookie(cookie);
    }

    /**
     * 读取用户的登陆令牌
     *
     * @param request
     * @return 返回令牌
     */
    public static String getLoginCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                LOGGER.info("get cookieName:{} value:{} ", COOKIE_NAME, cookie.getName());
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 删除保存的cookie
     *
     * @param response
     * @param token
     */
    public static void delLoginCookie(HttpServletResponse response, String token) {
        // cookie的name属性
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        // 将cookie的maxAge设置为0删除cookie
        cookie.setMaxAge(0);
        LOGGER.info("del cookieName:{} value:{} ", COOKIE_NAME, cookie.getName());
        response.addCookie(cookie);
    }

}
