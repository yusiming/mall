package com.mall.service;

import com.mall.common.ServerResponse;

/**
 * @Auther yusiming
 * @Date 2018/11/29 19:22
 */
public interface ICartService {
    /**
     * 根据用户id，向购物车中添加商品
     *
     * @param userId    用户id
     * @param productId 商品id
     * @param count     商品数量
     * @return 如果添加成功将购物车信息返回，否则返回错误的响应对象
     */
    ServerResponse add(Integer userId, Integer productId, Integer count);

    ServerResponse update(Integer userId, Integer productId, Integer count);

    ServerResponse delete(Integer userId, String productIds);

    ServerResponse list(Integer userId);

    ServerResponse selectOrUnSelect(Integer userId, Integer productId, Integer checked);

    ServerResponse getCartProductCount(Integer userId);

}
