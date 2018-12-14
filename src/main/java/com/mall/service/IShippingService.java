package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;

/**
 * @Auther yusiming
 * @Date 2018/11/30 19:17
 */
public interface IShippingService {
    /**
     * 用户添加收货地址
     *
     * @param userId   用户id
     * @param shipping 简单对象
     * @return 如果添加收货地址成功，返回成功的提示信息，否则返回错误的提示信息
     */
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse list(Integer id, int pageNum, int pageSize);
}
