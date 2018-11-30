package com.mall.service;

import com.mall.common.ServerResponse;

/**
 * @Auther yusiming
 * @Date 2018/11/29 19:22
 */
public interface ICartService {
    ServerResponse add(Integer userId, Integer productId, Integer count);

    ServerResponse update(Integer userId, Integer productId, Integer count);

    ServerResponse delete(Integer userId, String productIds);

    ServerResponse list(Integer userId);

    ServerResponse selectOrUnSelect(Integer userId, Integer productId, Integer checked);

    ServerResponse getCartProductCount(Integer userId);

}
