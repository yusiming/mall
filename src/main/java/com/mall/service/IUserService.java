package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.User;

/**
 * @author yusiming
 * @date 2018/11/22 21:34
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

    /**
     * 通过用户名称获取密码提示问题
     *
     * @param username 用户名
     * @return 如果该用户不存在、问题为空，返回错误响应，否则返回正确的响应
     */
    ServerResponse selectQuestion(String username);

    /**
     * 检验密码提示问题的答案是否正确
     *
     * @param username 用户名称
     * @param question 问题
     * @param answer   答案
     * @return 响应
     */
    ServerResponse checkAnswer(String username, String question, String answer);

    /**
     * 未登陆状态下，修改用户密码
     * 前端需要传入token值，来保证安全性
     *
     * @param username    用户名称
     * @param passwordNew 新密码
     * @param forgetToken token值
     * @return 响应
     */
    ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken);

    /**
     * 登陆状态下重置密码
     *
     * @param userId      用户id
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return 响应
     */
    ServerResponse resetPassword(Integer userId, String passwordOld, String passwordNew);

    /**
     * 登陆状态下更新用户信息
     *
     * @param user user对象
     * @return 响应
     */
    ServerResponse updateUserInfo(User user);

    /**
     * 根据userId获取用户详细信息
     *
     * @param userId 用户id
     * @return 响应
     */
    ServerResponse getInformation(Integer userId);

    /**
     * 检查是否为管理员
     *
     * @param user user对象
     * @return 响应
     */
    ServerResponse checkAdminRole(User user);
}
