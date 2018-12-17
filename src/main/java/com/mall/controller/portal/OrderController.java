package com.mall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Auther yusiming
 * @Date 2018/12/2 19:00
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    /**
     * 用户创建订单
     * 前台只需要传递shippingId，收货地址即可，所有的其他会通过当前登陆的用户来获取
     *
     * @param session    session域
     * @param shippingId 用户收货地址信息
     * @return 响应
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iOrderService.createOrder(response.getData().getId(), shippingId);
        }
        return response;
    }

    /**
     * 用户取消订单
     *
     * @param session session域
     * @param orderNo 订单编号
     * @return 响应
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, long orderNo) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iOrderService.cancel(response.getData().getId(), orderNo);
        }
        return response;
    }

    /**
     * 生成订单之前获取订单的信息（购物车中已勾选的商品所生成的信息），给用户展示，确认订单
     *
     * @param session session域
     * @return 响应
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            return iOrderService.getOrderCartProduct(response.getData().getId());
        }
        return response;
    }

    /**
     * 支付宝支付接口
     * 订单支付
     *
     * @param session session域
     * @param orderNo 订单号
     * @return 响应
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, long orderNo) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            String path = session.getServletContext().getRealPath("upload");
            return iOrderService.pay(response.getData().getId(), orderNo, path);
        }
        return response;
    }

    /**
     * 支付宝回调接口
     *
     * @param request request
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        // 获取请求所有的请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> map = Maps.newHashMap();
        for (String s : parameterMap.keySet()) {
            String valueStr = "";
            String[] values = parameterMap.get(s);
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 添加到参数封装到map中
            map.put(s, valueStr);
        }
        LOGGER.info("支付宝回调,sign:{},trade_status:{},参数:{}", map.get("sign"), map.get("trade_status"), map.toString());

        // 这里要移除sign_type，不然验签无法不通过，sign参数会被支付宝的代码移除
        map.remove("sign_type");
        try {
            /*
             * 使用验签方法验证回调的正确性，而且还要避免重复通知
             * 注意需要使用可以选择签名算法类型的验签方法，使用支付宝提供的Configs对象获取支付宝公钥和签名算法类型
             */
            if (!AlipaySignature.rsaCheckV2(map, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType())) {
                // 如果验证不通过，返回警告信息
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，再恶意将移交网警处理！");
            }
        } catch (AlipayApiException e) {
            LOGGER.error("支付宝验证回调异常", e);
        }
        // 内部校验通知数据的正确性，如果校验失败，忽略此次回调通知
        if (!iOrderService.checkTrade(map).isSuccess()) {
            return Const.AlipayCallback.RESPONSE_FAILED;
        }

        ServerResponse serverResponse = iOrderService.aliCallback(map);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 查询订单支付状态，订单知否已经支付成功
     *
     * @param session session
     * @param orderNo 订单号
     * @return 响应
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderPayStatus(HttpSession session, long orderNo) {
        ServerResponse<User> response = checkLogin(session);
        if (response.isSuccess()) {
            if (iOrderService.queryOrderPayStatus(response.getData().getId(), orderNo).isSuccess()) {
                return ServerResponse.createBySuccess(true);
            }
            return ServerResponse.createBySuccess(false);
        }
        return response;
    }

    /**
     * 获取订单详情
     *
     * @param session
     * @param orderNo
     * @return
     */
    public ServerResponse detail(HttpSession session, long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.getOrderDetail(user.getId(), orderNo);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 查看订单列表
     *
     * @param session  session域
     * @param pageNum  第几页
     * @param pageSize 每页数据的大小
     */
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 订单发货
     *
     * @param session session域
     * @param orderNo 订单号
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session, long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.manageSendGoods(orderNo);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
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
