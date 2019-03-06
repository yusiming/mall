package com.mall.controller.common;

import com.mall.common.Const;
import com.mall.pojo.User;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.ShardedRedisPoolUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 重置session的过期时间
 *
 * @author yusiming
 * @date 2019/3/5 22:50
 */
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = CookieUtil.getLoginCookie(request);
        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(servletRequest, servletResponse);
            // 这里必须要返回，否则会放行两次
            return;
        }
        String userJsonStr = ShardedRedisPoolUtil.get(token);
        if (StringUtils.isNotBlank(userJsonStr)) {
            User user = JsonUtil.stringToObj(userJsonStr, User.class);
            if (user != null) {
                ShardedRedisPoolUtil.expire(token, Const.SessionExTime.TIME);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
