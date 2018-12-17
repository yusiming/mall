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

    /**
     * 支付宝回调通过之后，修改订单的状态，将支付信息持久化到数据库中
     *
     * @param params 参数
     * @return 如果成功，返回正确的响应，如果失败返回错误的响应
     */
    ServerResponse aliCallback(Map<String, String> params);

    /**
     * 校验支付宝通知数据的正确性
     *
     * @param map 包含了通知数据的map
     * @return 如果校验通过，返回成功的响应，否则返回错误的响应
     */
    ServerResponse checkTrade(Map<String, String> map);

    /**
     * 根据用户id和订单号查询订单支付状态
     *
     * @param userId  用户id
     * @param orderNo 订单号
     * @return 如果订单支付成功，返回成功的响应，否则返回错误的响应
     */
    ServerResponse queryOrderPayStatus(Integer userId, long orderNo);

    /**
     * 通过用户id和收货地址id创建订单
     *
     * @param userId     用户id
     * @param shippingId 收货地址id
     * @return 如果创建成功，给前台返回包含了OrderVo的响应，否则返回错误的响应
     */
    ServerResponse createOrder(Integer userId, Integer shippingId);

    /**
     * 根据用户id和订单编号取消订单
     *
     * @param userId  用户id
     * @param orderNO 订单编号
     * @return 如果取消成功，返回正确的提示信息，否则返回错误的提示信息
     */
    ServerResponse cancel(Integer userId, long orderNO);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId, long orderNo);

    ServerResponse getOrderList(Integer userId, int pageNum, int pageSize);

    ServerResponse manageList(int pageNum, int pageSize);

    ServerResponse manageDetail(long orderNo);

    ServerResponse manageSearch(long orderNo, int pageNum, int pageSize);

    ServerResponse manageSendGoods(long orderNo);
}
