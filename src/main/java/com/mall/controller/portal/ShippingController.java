package com.mall.controller.portal;

import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;
import com.mall.pojo.User;
import com.mall.service.IShippingService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.ShardedRedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 收货地址接口
 *
 * @author yusiming
 * @date 2018/11/30 19:16
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {
    @Autowired
    private IShippingService iShippingService;

    /**
     * 校验用户是否已登陆
     *
     * @param httpServletRequest httpServletRequest
     * @return 如果用户已经登陆，返回成功的响应，否则返回错误的响应
     */
    private ServerResponse<User> checkLogin(HttpServletRequest httpServletRequest) {
        String token = CookieUtil.getLoginCookie(httpServletRequest);
        if (token != null) {
            User user = JsonUtil.stringToObj(ShardedRedisPoolUtil.get(token), User.class);
            if (user != null) {
                return ServerResponse.createBySuccess(user);
            }
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 用户添加收货地址
     * 使用springMVC的对象数据绑定
     *
     * @param request  request
     * @param shipping 收货地址对象
     * @return 响应
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpServletRequest request, Shipping shipping) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iShippingService.add(response.getData().getId(), shipping);
        }
        return response;
    }

    /**
     * 删除用户收货地址
     *
     * @param request    request
     * @param shippingId 收货地址id
     * @return 响应
     */
    @RequestMapping("del.do")
    @ResponseBody
    public ServerResponse del(HttpServletRequest request, Integer shippingId) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iShippingService.del(response.getData().getId(), shippingId);
        }
        return response;
    }

    /**
     * 更新用户地址信息
     *
     * @param request  request
     * @param shipping 代表收货地址的简单对象
     * @return 响应
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpServletRequest request, Shipping shipping) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iShippingService.update(response.getData().getId(), shipping);
        }
        return response;

    }

    /**
     * 查询地址详情
     *
     * @param request    request
     * @param shippingId 代表收货地址的简单对象
     * @return 响应
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse select(HttpServletRequest request, Integer shippingId) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iShippingService.select(response.getData().getId(), shippingId);
        }
        return response;
    }

    /**
     * 用户收货地址列表
     *
     * @param request  request
     * @param pageNum  第几页
     * @param pageSize 每页几条数据
     * @return 响应
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iShippingService.list(response.getData().getId(), pageNum, pageSize);
        }
        return response;
    }

}
