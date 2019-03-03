package com.mall.service;

import com.mall.common.ServerResponse;

import java.util.Map;

/**
 * @author yusiming
 * @date 2018/12/2 19:04
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

    /**
     * 获取订单信息，订单确认时需要使用
     *
     * @param userId 用户id
     * @return 响应
     */
    ServerResponse getOrderCartProduct(Integer userId);

    /**
     * 根据用户id和订单号获取订单的详细信息（OrderVo）
     *
     * @param userId  用户id
     * @param orderNo 订单编号
     * @return 如果获取成功，返回包含了订单信息（OrderVO）的响应，否则返回错误的响应
     */
    ServerResponse getOrderDetail(Integer userId, long orderNo);

    /**
     * 根据用户id，获取该用户的订单列表
     *
     * @param userId   用户id
     * @param pageNum  第几页
     * @param pageSize 每页几条数据
     * @return 响应
     */
    ServerResponse getOrderList(Integer userId, int pageNum, int pageSize);

    /**
     * 管理员获取订单列表
     *
     * @param pageNum  第几页
     * @param pageSize 每页几条数据
     * @return 响应
     */
    ServerResponse manageList(int pageNum, int pageSize);

    /**
     * 管理员获取订单详细信息
     *
     * @param orderNo 订单编号
     * @return 响应
     */
    ServerResponse manageDetail(long orderNo);

    /**
     * 管理员搜索订单
     *
     * @param orderNo  订单号
     * @param pageNum  第几页
     * @param pageSize 每页几条记录
     * @return 响应
     */
    ServerResponse manageSearch(long orderNo, int pageNum, int pageSize);

    /**
     * 管理员订单发货
     *
     * @param orderNo 订单号
     * @return 响应
     */
    ServerResponse manageSendGoods(long orderNo);
}
