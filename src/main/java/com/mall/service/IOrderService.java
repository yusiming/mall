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
}
