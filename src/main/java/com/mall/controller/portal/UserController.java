package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.RedisPoolUtil;
import com.mall.util.UUIDUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 前台用户接口
 *
 * @author yusiming
 * @date 2018/11/22 21:27
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登陆，限制为post请求
     *
     * @param username 用户名称
     * @param password 用户密码
     * @return 如果登陆成功，返回一个成功的响应，如果登陆失败返回一个错误的响应
     */
    @RequestMapping(value = "login.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse login(HttpServletResponse httpServletResponse, HttpServletRequest request, String username, String password) {
        /*
         * 1.如果从redis缓存中拿到了用户信息，证明已经登陆，无需从数据库中查询
         * 2.如果没有拿到信息，校验用户名称、密码是否正确
         * 3.如果校验通过将用户信息序列化后放入Redis缓存中，同时向客户端注入cookie
         * 4.如果校验未通过，返回提示信息
         */
        String userJsonStr = RedisPoolUtil.get(CookieUtil.getLoginCookie(request));
        if (StringUtils.isNotBlank(userJsonStr)) {
            return ServerResponse.createBySuccessMsg("请勿重复登陆！");
        }
        ServerResponse response = iUserService.login(username, password);
        if (response.isSuccess()) {
            String token = UUIDUtil.randomUUID();
            CookieUtil.sendLoginCookie(httpServletResponse, token);
            RedisPoolUtil.setEx(token, JsonUtil.objToString(response.getData()), Const.SessionExTime.TIME);
        }
        return response;
    }

    /**
     * 用户登出
     *
     * @param request  请求
     * @param response 响应
     * @return 如果用户未登陆，返回提示信息，如果已登陆返回成功提示
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse logout(HttpServletRequest request, HttpServletResponse response) {
        /*
         * 1.删除向用户端注入的cookie
         * 2.如果cookie不存在，则证明用户尚未登陆
         * 3.如果cookie存在，根据token值删除redis缓存中的用户信息
         */
        String token = CookieUtil.delLoginCookie(request, response);
        if (token == null) {
            return ServerResponse.createByErrorMessage("您还未登陆！");
        }
        RedisPoolUtil.del(token);
        return ServerResponse.createBySuccess();
    }

    /**
     * 注册
     *
     * @param user 参数绑定
     * @return 响应
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse register(User user) {
        return iUserService.register(user);
    }

    /**
     * 校验用户名称和email
     *
     * @param str  校验的值
     * @param type 需要校验的类型
     * @return 响应
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse checkValid(String type, String str) {
        return iUserService.checkValid(type, str);
    }

    /**
     * 获取登陆用户信息
     *
     * @param httpServletRequest 请求
     * @return 如果用户未登陆，返回提示信息，否则返回用户信息
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getUserInfo(HttpServletRequest httpServletRequest) {
        ServerResponse<User> response = checkLogin(httpServletRequest);
        if (response.isSuccess()) {
            return ServerResponse.createBySuccess(response.getData());
        }
        return response;
    }

    /**
     * 通过用户名获取密码提示问题
     *
     * @param username 用户名称
     * @return 响应
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 检测密码提示问题的答案是否正确
     *
     * @param username 用户名
     * @param question 问题
     * @param answer   答案
     * @return 响应
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 未登陆状态下，通过密码提示问题重置用户密码
     *
     * @param username    用户名称
     * @param passwordNew 新密码
     * @param forgetToken token值
     * @return 响应
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登陆状态下重置用户密码
     *
     * @param request     session
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return 响应
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse resetPassword(HttpServletRequest request, String passwordOld, String passwordNew) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iUserService.resetPassword(response.getData().getId(), passwordOld, passwordNew);
        }
        return response;
    }

    /**
     * 登陆状态下更新用户信息
     *
     * @param request request
     * @param user    User对象
     * @return 响应
     */
    @RequestMapping(value = "update_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse updateUserInfo(HttpServletRequest request, User user) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            User currentUser = response.getData();
            // 防止用户越权，将user对象的id设置为session域中user的id，这样就保证了user只能改自己的信息
            user.setId(currentUser.getId());
            ServerResponse updateResponse = iUserService.updateUserInfo(user);
            if (updateResponse.isSuccess()) {
                String token = CookieUtil.getLoginCookie(request);
                RedisPoolUtil.set(token, JsonUtil.objToString(user));
            }
            return updateResponse;
        }
        return response;
    }

    /**
     * 获取登陆用户详细信息
     *
     * @param request 请求
     * @return 如果用户已登陆返回用户信息，如果未登陆，返回提示信息
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getInformation(HttpServletRequest request) {
        /*
         * 1.判断用户是否登陆
         * 2.如果已登陆，根据用户id从数据库中获取详细信息
         * 3.如果未登陆，返回提示信息
         */
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iUserService.getInformation(response.getData().getId());
        }
        return response;
    }

    /**
     * 校验用户是否已经登陆
     *
     * @param httpServletRequest request
     * @return 如果已经登陆，将用户信息返回，如果未登陆，返回提示信息
     */
    private ServerResponse<User> checkLogin(HttpServletRequest httpServletRequest) {
        String token = CookieUtil.getLoginCookie(httpServletRequest);
        if (token != null) {
            User user = JsonUtil.stringToObj(RedisPoolUtil.get(token), User.class);
            if (user != null) {
                return ServerResponse.createBySuccess(user);
            }
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }
}
