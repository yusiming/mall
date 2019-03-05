package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author yusiming
 * @date 2018/11/23 13:16
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {
    @Autowired
    private IUserService iUserService;

    /**
     * 后台管理员登陆
     *
     * @param username 用户名称
     * @param password 密码
     * @param session  session域
     * @return 响应
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse) {
        ServerResponse response = iUserService.login(username, password);
        if (response.isSuccess()) {
            User user = (User) response.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                // 向客户端发送一个 name为login_cookie , value 为 UUID 的cookie
                CookieUtil.sendLoginCookie(httpServletResponse, session.getId());
                // 将用户信息保存到Redis服务器中
                RedisPoolUtil.setEx(session.getId(), JsonUtil.objToString(response.getData()), Const.SessionExTime.TIME);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员无法登陆");
            }
        }
        return response;
    }
}
