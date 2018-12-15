package com.mall.service;

import com.mall.common.ServerResponse;

import java.util.Map;

/**
 * @Auther yusiming
 * @Date 2018/12/2 19:04
 */
public interface IOrderService {
    /**
     * 用户支付订单
     *
     * @param userId  用户id
     * @param orderNo 订单号
     * @param path    二维码上传路径
     * @return 如果支付成功，给前台返回订单号和二维码图片完整地址，否则返回错误的信息
     */
    ServerResponse pay(Integer userId, long orderNo, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, long orderNo);

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse cancel(Integer userId, long orderNO);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId, long orderNo);

    ServerResponse getOrderList(Integer userId, int pageNum, int pageSize);

    ServerResponse manageList(int pageNum, int pageSize);

    ServerResponse manageDetail(long orderNo);

    ServerResponse manageSearch(long orderNo, int pageNum, int pageSize);

    ServerResponse manageSendGoods(long orderNo);
}
