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
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.add(user.getId(), productId, count);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 更新购物车中的商品数量
     *
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Integer productId, Integer count) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.update(user.getId(), productId, count);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 删除购物车中的商品
     *
     * @param session
     * @param productIds
     * @return
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(HttpSession session, String productIds) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.delete(user.getId(), productIds);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 查询购物车
     *
     * @param session
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iCartService.list(user.getId());
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
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

}
