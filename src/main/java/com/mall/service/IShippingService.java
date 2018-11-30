package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;

/**
 * @Auther yusiming
 * @Date 2018/11/30 19:17
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse list(Integer id, int pageNum, int pageSize);
}
