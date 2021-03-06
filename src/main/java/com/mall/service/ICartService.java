package com.mall.service;

import com.mall.common.ServerResponse;

/**
 * @author yusiming
 * @date 2018/11/29 19:22
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

    /**
     * 根据用户id，更新购物车中商品的数量
     *
     * @param userId    用户id
     * @param productId 商品id
     * @param count     商品数量
     * @return 如果更新成功，返回包含了该用户的购物车数据，否则返回错误的响应对象
     */
    ServerResponse update(Integer userId, Integer productId, Integer count);

    /**
     * 根据用户id和商品id，删除该用户对应的购物车记录
     *
     * @param userId     用户id
     * @param productIds 商品id
     * @return 如果删除成功，返回包含了该用户的购物车数据，否则返回错误的响应对象
     */
    ServerResponse delete(Integer userId, String productIds);

    /**
     * 根据用户id获取用户的购物车数据
     *
     * @param userId 用户id
     * @return 包含了购物车数据的响应对象
     */
    ServerResponse list(Integer userId);

    /**
     * 设置购物车中商品的选中状态
     *
     * @param userId    用户id
     * @param productId 商品id
     * @param checked   0代表不选，1代表选中
     * @return 响应
     */
    ServerResponse selectOrUnSelect(Integer userId, Integer productId, Integer checked);

    /**
     * 获取用户的购物车中的商品数量
     *
     * @param userId 用户id
     * @return 响应
     */
    ServerResponse getCartProductCount(Integer userId);

}
