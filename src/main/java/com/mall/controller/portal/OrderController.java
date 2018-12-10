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
     *
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.createOrder(user.getId(), shippingId);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 取消订单
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.cancel(user.getId(), orderNo);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 获取购物车中商品信息
     *
     * @param session
     * @return
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.getOrderCartProduct(user.getId());
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 支付宝支付接口
     *
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            String path = session.getServletContext().getRealPath("upload");
            return iOrderService.pay(user.getId(), orderNo, path);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 支付宝回调接口
     *
     * @param request request
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipyCallback(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> map = Maps.newHashMap();
        for (String s : parameterMap.keySet()) {
            String valueStr = "";
            String[] values = parameterMap.get(s);
            for (int i = 0; i < values.length; i++) {
                valueStr = (i != values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            map.put(s, valueStr);
        }
        LOGGER.info("支付宝回调,sign:{},trade_status:{},参数:{}", map.get("sign"), map.get("trade_status"), map.toString());

        // 这里要移除sign_type
        map.remove("sign_type");
        try {
            // 验证回调的正确性，验证回调到底是不是支付宝发的，而且还要避免重复通知
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(map, Configs.getAlipayPublicKey(), "utf-8", Configs
                    .getSignType());
            // 如果验证通过
            if (!alipayRSACheckV2) {
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，在恶意请求我就找网警了");
            }
        } catch (AlipayApiException e) {
            LOGGER.error("支付宝验证回调异常", e);
        }
        // TODO: 2018/12/2 验证各种数据，比如订单号是否正确，订单总金额是否正确

        //
        ServerResponse serverResponse = iOrderService.aliCallback(map);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 查询订单支付状态
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderPayStatus(HttpSession session, long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
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

}