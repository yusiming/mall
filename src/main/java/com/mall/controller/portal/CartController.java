package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 前台购物车接口
 *
 * @Auther yusiming
 * @Date 2018/11/29 19:17
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService iCartService;

    /**
     * 向购物车中添加商品
     *
     * @param session   session域
     * @param productId 商品id
     * @param count     商品数量
     * @return 响应对象
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session, Integer productId, Integer count) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iCartService.add(response.getData().getId(), productId, count);
        }
        return response;
    }

    /**
     * 更新购物车中的商品数量
     *
     * @param session   session域
     * @param productId 商品id
     * @param count     商品的数量
     * @return 响应
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Integer productId, Integer count) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iCartService.update(response.getData().getId(), productId, count);
        }
        return response;
    }

    /**
     * 删除购物车中的商品，有可能传入多个商品id
     *
     * @param session    session域
     * @param productIds 商品id
     * @return 响应
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(HttpSession session, String productIds) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iCartService.delete(response.getData().getId(), productIds);
        }
        return response;
    }

    /**
     * 查询用户的购物车数据
     *
     * @param session session
     * @return 响应
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iCartService.list(response.getData().getId());
        }
        return response;
}

    /**
     * 购物车全选
     *
     * @param session session域
     * @return
     */
    @RequestMapping("selectAll.do")
    @ResponseBody
    public ServerResponse selectAll(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.CHECKED);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 购物车全不选
     *
     * @param session session域
     * @return
     */
    @RequestMapping("un_selectAll.do")
    @ResponseBody
    public ServerResponse unSelectAll(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.UN_CHECKED);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 单独选
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse Select(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.CHECKED);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 单独反选
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse unSelect(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.UN_CHECKED);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 获取用户购物车中的商品数量
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("get_product_count.do")
    @ResponseBody
    public ServerResponse getProductCount(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.getCartProductCount(user.getId());
        }
        return ServerResponse.createBySuccess(0);
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
