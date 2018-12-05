package com.mall.service;

import com.mall.common.ServerResponse;

import java.util.Map;

/**
 * @Auther yusiming
 * @Date 2018/12/2 19:04
 */
public interface IOrderService {
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
