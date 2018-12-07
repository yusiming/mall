package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.User;

/**
 * @Auther yusiming
 * @Date 2018/11/22 21:34
 */
public interface IUserService {
    /**
     * 校验用户登陆信息
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果校验通过，返回一个成功的响应，否则返回错误的响应
     */
    ServerResponse login(String username, String password);

    /**
     * 用户注册
     *
     * @param user 代表用户注册信息的User对象
     * @return 如果注册成功返回成功的响应，否则返回失败的响应
     */
    ServerResponse register(User user);

    /**
     * 校验用户名和email是否存在
     *
     * @param type 校验的类型，username或者为email
     * @param str  需要校验的值
     * @return 如果存在，返回错误的响应，否则返回成功的响应
     */
    ServerResponse checkValid(String type, String str);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);

    ServerResponse<User> updateUserInfo(User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse<String> checkAdminRole(User user);
}
