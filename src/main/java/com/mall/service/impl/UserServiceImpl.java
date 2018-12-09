package com.mall.service.impl;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.common.TokenCache;
import com.mall.dao.UserMapper;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.MD5Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @Auther yusiming
 * @Date 2018/11/22 21:37
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 校验用户登陆信息
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果登陆成功返回正确响应，将user对象返回data域中返回给前端，否则返回错误响应
     */
    @Override
    public ServerResponse login(String username, String password) {
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByErrorMessage("用户名不能为空!");
        }
        /*
         * 校验逻辑：
         * 1.先校验用户名称，如果数据库中不存在该用户名，直接返回一个错误的响应对象
         * 2.根据用户名和密码查询用户，如果查询结果为null，证明密码错误
         * 如果查询结果不为null，则返回ServerResponse<User>对象，
         */
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在!");
        }
        // 注意：数据库中存储的是经过MD5加密之后的密码
        password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误!");
        }
        // 抹去密码
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessMsg("登陆成功!", user);
    }

    /**
     * 用户注册
     *
     * @param user 代表用户注册信息的User对象
     * @return 响应
     */
    public ServerResponse register(User user) {
        /*
         * 用户注册逻辑：
         * 1.校验用户名称
         * 2.校验email
         * 3.设置用户角色
         * 4.处理密码
         * 5.插入记录到数据库中
         */
        ServerResponse response = checkValid(Const.USERNAME, user.getUsername());
        if (!response.isSuccess()) {
            return response;
        }
        response = checkValid(Const.EMAIL, user.getEmail());
        if (!response.isSuccess()) {
            return response;
        }
        // 设置用户角色为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    /**
     * 校验用户名和email
     *
     * @param str  校验的值
     * @param type 校验的类型
     * @return 如果对应的值不存在，返回成功的响应，否则返回失败的响应
     */
    @Override
    public ServerResponse checkValid(String type, String str) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在!");
                }
            } else if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已被注册!");
                }
            } else {
                return ServerResponse.createByErrorMessage("参数错误!");
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误!");
        }
        return ServerResponse.createBySuccessMsg("校验成功!");
    }

    /**
     * 根据用户名查找密码提示问题
     *
     * @param username 用户名
     * @return 如果该用户不存在、问题为空，返回错误响应，否则返回正确的响应
     */
    public ServerResponse selectQuestion(String username) {
        /*
         * 1.校验用户名称
         * 2.根据用户名称查找问题
         * 3.返回给前端
         */
        ServerResponse validResponse = checkValid(Const.USERNAME, username);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        // 注意null、""、" " 都需要返回错误响应
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("没有设置找回密码问题");
    }

    /**
     * 校验找回密码问题的答案
     *
     * @param username 用户名称
     * @param question 找回密码问题
     * @param answer   用户填写的答案
     * @return 响应
     */
    public ServerResponse checkAnswer(String username, String question, String answer) {
        /*
         * 校验答案：
         * 1.如果答案错误返回错误响应
         * 2.如果答案正确，给前端返回一个"token"令牌，该令牌在服务器端会保留12个小时
         *   用户在修改密码时需要传递过来这个"token"令牌
         */
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 重置用户密码
     *
     * @param username    用户名
     * @param passwordNew 新密码
     * @param forgetToken token
     * @return 响应
     */
    public ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken) {
        /*
         * 1.校验token值是否为空
         * 2.校验用户名称是否存在
         * 3.校验服务器端token值是否存在
         * 4.校验两个token值是否一致
         * 5.修改密码
         */
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token为空");
        }
        ServerResponse validResponse = this.checkValid(Const.USERNAME, username);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token不存在或者过期，请重新获取token");
        }
        if (StringUtils.equals(forgetToken, token)) {
            passwordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, passwordNew);
            if (rowCount > 0) {
                // 修改密码成功，将token删除
                TokenCache.removeKey(TokenCache.TOKEN_PREFIX + username);
                return ServerResponse.createBySuccessMsg("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 更新用户密码
     *
     * @param userId      用户id
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return 响应
     */
    public ServerResponse resetPassword(Integer userId, String passwordOld, String passwordNew) {
        // 防止横向越权，需要校验一下这个用户的密码
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), userId);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        resultCount = userMapper.updatePasswordByUserId(userId,passwordNew);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 更新用户信息
     *
     * @param user user对象
     * @return 响应
     */
    public ServerResponse updateUserInfo(User user) {
        // username不能被更新
        // 需要校验email是否已经被别的用户使用了
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已经被注册");
        }
        // 只更新email、phone、question、answer字段
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setUpdateTime(new Date());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            updateUser.setUsername(user.getUsername());
            updateUser.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccessMsg("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更细个人信息失败");
    }

    /**
     * 根据用户id获取用户详细信息
     *
     * @param userId 用户id
     * @return 响应对象
     */
    public ServerResponse getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        // 抹去密码
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 校验用户是否为管理员
     *
     * @param user 需要校验的用户
     * @return 响应对象
     */
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
