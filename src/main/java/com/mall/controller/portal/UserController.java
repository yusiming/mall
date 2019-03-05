package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
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
     * @param session  session域
     * @param username 用户名称
     * @param password 用户密码
     * @return 如果登陆成功，返回一个成功的响应，如果登陆失败返回一个错误的响应
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse login(HttpSession session, HttpServletResponse httpServletResponse, String username, String password) {
        ServerResponse response = iUserService.login(username, password);
        if (response.isSuccess()) {
            // 向客户端发送一个 name为login_cookie , value 为 UUID 的cookie
            CookieUtil.sendLoginCookie(httpServletResponse, session.getId());
            // 将用户信息保存到Redis服务器中
            RedisPoolUtil.setEx(session.getId(), JsonUtil.objToString(response.getData()), Const.SessionExTime.TIME);
        }
        // 无论是否登陆成功，都返回response，如果成功，前台拿到信息可以显示到页面上，如果登陆失败，显示错误信息
        return response;
    }

    /**
     * 用户登出，将session域中user对象删除即可
     *
     * @param session session域
     * @return 响应
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse logout(HttpSession session, HttpServletResponse response) {
        CookieUtil.delLoginCookie(response, session.getId());
        RedisPoolUtil.del(session.getId());
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
     * 从session中获取登陆用户信息
     *
     * @param session session域对象
     * @return 若用户未登陆，返回错误的响应对象，否则返回正确的响应对象，将user的信息传递给前端
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getUserInfo(HttpSession session) {
        ServerResponse<User> response = checkLogin(session);
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
     * @param session     session
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return 响应
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iUserService.resetPassword(response.getData().getId(), passwordOld, passwordNew);
        }
        return response;
    }

    /**
     * 登陆状态下更新用户信息
     *
     * @param session session域
     * @param user    User对象
     * @return 响应
     */
    @RequestMapping(value = "update_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse updateUserInfo(HttpSession session, User user) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            User currentUser = response.getData();
            // 防止用户越权，将user对象的id设置为session域中user的id，这样就保证了user只能改自己的信息
            user.setId(currentUser.getId());
            user.setUsername(currentUser.getUsername());
            ServerResponse updateResponse = iUserService.updateUserInfo(user);
            if (updateResponse.isSuccess()) {
                // 在session域中放入更新过的用户信息
                session.setAttribute(Const.CURRENT_USER, response.getData());
            }
            return updateResponse;
        }
        return response;
    }

    /**
     * 获取当前登陆用户详细信息
     *
     * @param session session域
     * @return 响应
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getInformation(HttpSession session) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iUserService.getInformation(response.getData().getId());
        }
        return response;
    }

    /**
     * 校验用户是否已登陆
     *
     * @param session session域
     * @return 如果用户已经登陆，返回成功的响应，否则返回错误的响应
     */
    private ServerResponse<User> checkLogin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return ServerResponse.createBySuccess(user);
    }
}
