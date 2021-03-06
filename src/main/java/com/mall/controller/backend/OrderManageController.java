package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
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
 * @author yusiming
 * @date 2018/12/5 21:50
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {
    @Autowired
    private IOrderService iOrderService;

    /**
     * 校验用户是否为管理员
     *
     * @param request request
     * @return 如果用户未登陆或者不是管理员，返回错误的响应，否则返回成功的响应
     */
    private ServerResponse checkAdmin(HttpServletRequest request) {
        String token = CookieUtil.getLoginCookie(request);
        if (token != null) {
            User user = JsonUtil.stringToObj(ShardedRedisPoolUtil.get(token), User.class);
            if (user != null && user.getRole().equals(Const.Role.ROLE_ADMIN)) {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByErrorMessage("您不是管理员，请勿随意登陆!");
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 管理员查看订单列表
     *
     * @param request  request
     * @param pageNum  第几页
     * @param pageSize 每页几条数据
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse orderList(HttpServletRequest request,
                                    @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iOrderService.manageList(pageNum, pageSize);
        }
        return response;
    }

    /**
     * 查询商品详情
     *
     * @param request request
     * @param orderNo 订单号
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse orderDetail(HttpServletRequest request, long orderNo) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iOrderService.manageDetail(orderNo);
        }
        return response;
    }

    /**
     * 管理员搜索订单
     *
     * @param request  request
     * @param orderNo  订单号
     * @param pageNum  第几页
     * @param pageSize 每页几条数据
     * @return 响应
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse orderSearch(HttpServletRequest request, long orderNo,
                                      @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        }
        return response;
    }

    /**
     * 管理员订单发货
     *
     * @param request request
     * @param orderNo 订单号
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpServletRequest request, long orderNo) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iOrderService.manageSendGoods(orderNo);
        }
        return response;
    }
}
