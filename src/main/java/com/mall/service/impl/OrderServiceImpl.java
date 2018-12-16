package com.mall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.dao.*;
import com.mall.pojo.*;
import com.mall.service.IOrderService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.DateTimeUtil;
import com.mall.util.FTPUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.OrderItemVo;
import com.mall.vo.OrderProductVo;
import com.mall.vo.OrderVo;
import com.mall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Auther yusiming
 * @Date 2018/12/2 19:05
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
    /**
     * AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
     */
    private static AlipayTradeService tradeService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    static {
        /*
         * 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         * Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        // 设置字符集
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().setCharset("utf-8").build();
    }

    /**
     * 用户支付订单
     *
     * @param userId  用户id
     * @param orderNo 订单号
     * @param path    二维码上传路径
     * @return 响应
     */
    @Override
    public ServerResponse pay(Integer userId, long orderNo, String path) {
        Map<String, String> map = Maps.newHashMap();
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        // 需要给前台返回orderNo
        map.put("orderNo", Long.toString(orderNo));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = Long.toString(orderNo);

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "mall扫码支付";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder("订单").append(outTradeNo).append("购买商品共").append(totalAmount)
                .append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        // 获取所有的订单所有的订单项
        List<OrderItem> orderItemList = orderItemMapper.selectAllByUserIdAndOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            // 商品id、商品名称、商品单价（单价为分）、商品数量
            goodsDetailList.add(GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), 100.0d).longValue(),
                    orderItem.getQuantity()));
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                // 支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))
                .setGoodsDetailList(goodsDetailList);

        // AlipayF2FPrecreateResult 支付宝的响应，执行到这一步已经拿到支付宝的响应了
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                LOGGER.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    if (!folder.setWritable(true)) {
                        LOGGER.warn("设置写权限失败！");
                    }
                    if (!folder.mkdirs()) {
                        LOGGER.error("创建文件夹失败!");
                    }
                }

                // 将二维码上传到图片服务器中，注意：我们这里获取的真实路径path最后是没有 "/" 的，
                String qrPath = String.format(path + "/qr-%s.png", orderNo);
                // 二维码图片文件名称,orderNo会替换到 %s 上
                String qrFilenName = String.format("qr-%s.png", orderNo);
                // 将内容contents生成长宽均为width的图片，图片路径由imgPath指定
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File targetFile = new File(qrPath, qrFilenName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    LOGGER.error("上传二维码异常", e);
                }
                LOGGER.info("filePath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                // 给前端返回二维码图片地址
                map.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(map);
            case FAILED:
                LOGGER.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
                LOGGER.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                LOGGER.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            LOGGER.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                LOGGER.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            LOGGER.info("body:" + response.getBody());
        }
    }

    /**
     * 支付宝回调通过之后，修改订单的状态，将支付信息持久化到数据库中
     *
     * @param params 参数
     * @return 响应
     */
    public ServerResponse aliCallback(Map<String, String> params) {
        // 订单号
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        // 支付宝交易号
        String tradeNo = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在，回调忽略");
        }
        // 如果该订单已经支付，则支付宝重复回调了，忽略即可
        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccessMsg("支付宝重复回调");
        }

        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            // 更新付款时间
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            // 设置订单状态为已支付
            order.setStatus(Const.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 创建交易信息对象
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatform.ALIPAY.getCode());
        // 设置支付宝交易号
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        // 持久化到数据库中
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    /**
     * 校验支付宝通知数据的正确性
     *
     * @param map 包含了通知数据的map
     * @return 响应
     */
    public ServerResponse checkTrade(Map<String, String> map) {
        /*
         * 1.校验out_trade_no是否为系统中创建的订单号
         * 2.判断total_amount是否确实为该订单的实际金额
         * 3.验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方
         *   有的时候，一个商户可能有多个seller_id/seller_email，我们这个项目中，seller_id/seller_email就只有一个
         */
        if (!Configs.getPid().equals(map.get("seller_id"))) {
            return ServerResponse.createByError();
        }
        if (orderMapper.selectOrderByOrderNoAndTotalAmount(map.get("out_trade_no"), map.get("total_amount")) > 0) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, long orderNo) {
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户该订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        ServerResponse serverResponse = this.getCartOrderItems(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        BigDecimal payment = getOrderTotalPrice(orderItemList);

        // 生成订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        // mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);
        // 订单生产成功，减少商品库存
        this.reduceProductStock(orderItemList);
        // 清空购物车
        this.cleanCart(cartList);
        // 返回给前端数据
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    private ServerResponse getCartOrderItems(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            // 校验商品数量
            if (Const.ProductStatus.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "非在线售卖状态");
            }
            // 校验商品库存
            if (cart.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        order.setOrderNo(this.generateOrderNo());
        order.setStatus(Const.OrderStatus.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentType.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        int rowCount = orderMapper.insert(order);
        if (rowCount >= 0) {
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     *
     * @return
     */
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    /**
     * 减少商品库存
     *
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            orderItemMapper.updateProductStock(orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            // 删除该条记录
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo ordervo = new OrderVo();
        ordervo.setOrderNo(order.getOrderNo());
        ordervo.setPayment(order.getPayment());
        ordervo.setPaymentType(order.getPaymentType());
        ordervo.setPaymentTypeDesc(Const.PaymentType.getDesc(order.getPaymentType()));
        ordervo.setPostage(order.getPostage());
        ordervo.setStatus(order.getStatus());
        ordervo.setStatusDesc(Const.OrderStatus.getDesc(order.getStatus()));
        order.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            ordervo.setReceiverName(shipping.getReceiverName());
            ordervo.setShippingVo(assembleShippingVo(shipping));
        }
        ordervo.setPaymentTime(DateTimeUtil.DateToStr(order.getPaymentTime()));
        ordervo.setSendTime(DateTimeUtil.DateToStr(order.getSendTime()));
        ordervo.setEndTime(DateTimeUtil.DateToStr(order.getEndTime()));
        ordervo.setCreateTime(DateTimeUtil.DateToStr(order.getCreateTime()));
        ordervo.setCloseTime(DateTimeUtil.DateToStr(order.getCloseTime()));
        ordervo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        ordervo.setOrderItemVoList(orderItemVoList);
        return ordervo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.DateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    /**
     * 取消用户订单
     *
     * @param userId
     * @param orderNO
     * @return
     */
    public ServerResponse cancel(Integer userId, long orderNO) {
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId, orderNO);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() != Const.OrderStatus.NO_PAY.getCode()) {
            // 这里是一个问题，可以通过对接支付宝的退款，完成用户的退款
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }
        Order updateOrder = new Order();
        order.setId(order.getId());
        order.setStatus(Const.OrderStatus.CANCEL.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        ServerResponse response = getCartOrderItems(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse getOrderDetail(Integer userId, long orderNo) {
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectAllByUserIdAndOrderNo(userId, orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("该订单不存在");
    }

    @Override
    public ServerResponse getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList;
            if (userId == null) {
                orderItemList = orderItemMapper.selectAllByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectAllByUserIdAndOrderNo(userId, order.getOrderNo());
            }
            orderVoList.add(assembleOrderVo(order, orderItemList));
        }
        return orderVoList;
    }

    // 后台管理模块
    public ServerResponse manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse manageDetail(long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectAllByOrderNo(orderNo);
        return ServerResponse.createBySuccess(assembleOrderVo(order, orderItemList));
    }

    public ServerResponse manageSearch(long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectAllByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        PageInfo result = new PageInfo(Lists.newArrayList(order));
        result.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(result);
    }

    @Override
    public ServerResponse manageSendGoods(long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null && order.getStatus() == Const.OrderStatus.PAID.getCode()) {
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setOrderNo(order.getOrderNo());
            updateOrder.setStatus(Const.OrderStatus.SHIPPED.getCode());
            updateOrder.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(updateOrder);
            return ServerResponse.createBySuccessMsg("发货成功!");
        }
        return ServerResponse.createByErrorMessage("订单异常，无法发货!");
    }
}
