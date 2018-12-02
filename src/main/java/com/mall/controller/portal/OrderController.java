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

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse queryOrderPayStatus(HttpSession session, long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
            if (serverResponse.isSuccess()) {
                return ServerResponse.createBySuccess(true);
            }
            return ServerResponse.createBySuccess(false);
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }
}
