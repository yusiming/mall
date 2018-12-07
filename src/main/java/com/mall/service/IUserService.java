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

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String type, String str);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);

    ServerResponse<User> updateUserInfo(User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse<String> checkAdminRole(User user);
}
