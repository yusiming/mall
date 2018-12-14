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

    /**
     * 根据用户id和收货地址id删除用户的收货地址
     *
     * @param userId     用户id
     * @param shippingId 说货地址id
     * @return 如果删除地址成功，返回成功的提示信息，否则返回失败的提示信息
     */
    ServerResponse del(Integer userId, Integer shippingId);

    /**
     * 根据用户id，更新用户的地址信息
     *
     * @param userId   用户id
     * @param shipping 地址对象
     * @return 如果更新地址成功，返回成功的提示信息，否则返回失败的提示信息
     */
    ServerResponse update(Integer userId, Shipping shipping);

    /**
     * 根据用户id，查询地址详情
     *
     * @param userId     用户id
     * @param shippingId 收货地址详情
     * @return 如果查询成功，返回包含了地址信息的响应对象，否则返回错误的响应信息
     */
    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse list(Integer id, int pageNum, int pageSize);
}
