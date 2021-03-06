package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICartService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.ShardedRedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 前台购物车接口
 *
 * @author yusiming
 * @date 2018/11/29 19:17
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService iCartService;

    /**
     * 向购物车中添加商品
     *
     * @param request   request对象
     * @param productId 商品id
     * @param count     商品数量
     * @return 响应对象
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpServletRequest request, Integer productId, Integer count) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.add(response.getData().getId(), productId, count);
        }
        return response;
    }

    /**
     * 更新购物车中的商品数量
     *
     * @param request   request
     * @param productId 商品id
     * @param count     商品的数量
     * @return 响应
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpServletRequest request, Integer productId, Integer count) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.update(response.getData().getId(), productId, count);
        }
        return response;
    }

    /**
     * 删除购物车中的商品，有可能传入多个商品id
     *
     * @param request    request
     * @param productIds 商品id
     * @return 响应
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(HttpServletRequest request, String productIds) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.delete(response.getData().getId(), productIds);
        }
        return response;
    }

    /**
     * 查询用户的购物车数据
     *
     * @param request request
     * @return 响应
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.list(response.getData().getId());
        }
        return response;
    }

    /**
     * 购物车商品全选
     *
     * @param request request
     * @return 响应
     */
    @RequestMapping("selectAll.do")
    @ResponseBody
    public ServerResponse selectAll(HttpServletRequest request) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            // 这里productId传入null，表示全部商品
            return iCartService.selectOrUnSelect(response.getData().getId(), null, Const.Cart.CHECKED);
        }
        return response;
    }

    /**
     * 购物车商品全不选
     *
     * @param request request
     * @return 响应
     */
    @RequestMapping("un_selectAll.do")
    @ResponseBody
    public ServerResponse unSelectAll(HttpServletRequest request) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            // 这里productId传入null，表示全部商品
            return iCartService.selectOrUnSelect(response.getData().getId(), null, Const.Cart.UN_CHECKED);
        }
        return response;
    }

    /**
     * 单独选中某个商品
     *
     * @param request   request
     * @param productId 商品id
     * @return 响应
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse Select(HttpServletRequest request, Integer productId) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.selectOrUnSelect(response.getData().getId(), productId, Const.Cart.CHECKED);

        }
        return response;
    }

    /**
     * 单独反选某个商品
     *
     * @param request   请求对象
     * @param productId 商品id
     * @return 响应
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse unSelect(HttpServletRequest request, Integer productId) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            return iCartService.selectOrUnSelect(response.getData().getId(), productId, Const.Cart.UN_CHECKED);

        }
        return response;
    }

    /**
     * 获取用户购物车中的商品数量
     *
     * @param request request
     * @return 响应
     */
    @RequestMapping("get_product_count.do")
    @ResponseBody
    public ServerResponse getProductCount(HttpServletRequest request) {
        ServerResponse<User> response = checkLogin(request);
        if (response.isSuccess()) {
            User user = response.getData();
            if (user != null) {
                return iCartService.getCartProductCount(user.getId());
            }
        }
        return ServerResponse.createBySuccess(0);
    }

    /**
     * 校验用户是否已登陆
     *
     * @param httpServletRequest 请求
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
}
