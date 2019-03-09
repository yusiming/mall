package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.ShardedRedisPoolUtil;
import com.mall.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     * @param request  request
     * @return 响应
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse login(HttpServletRequest request, HttpServletResponse httpServletResponse, String username, String password) {
        String userJsonStr = ShardedRedisPoolUtil.get(CookieUtil.getLoginCookie(request));
        User user = JsonUtil.stringToObj(userJsonStr, User.class);
        if (user != null && user.getRole().equals(Const.Role.ROLE_ADMIN)) {
            return ServerResponse.createBySuccessMsg("请勿重复登陆！");
        }
        ServerResponse response = iUserService.login(username, password);
        user = (User) response.getData();
        if (user != null && user.getRole().equals(Const.Role.ROLE_ADMIN)) {
            String token = UUIDUtil.randomUUID();
            CookieUtil.sendLoginCookie(httpServletResponse, token);
            ShardedRedisPoolUtil.setEx(token, JsonUtil.objToString(response.getData()), Const.SessionExTime.TIME);
        }
        return response;
    }
}
