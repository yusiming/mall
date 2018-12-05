package com.mall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 *
 * @Auther yusiming
 * @Date 2018/11/22 22:37
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface Role {
        //普通用户
        int ROLE_CUSTOMER = 0;
        // 管理员
        int ROLE_ADMIN = 1;
    }

    public enum ProductStatus {
        ON_SALE("在售", 1),;
        private String value;
        private int code;

        ProductStatus(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    public interface Cart {
        int UN_CHECKED = 0; // 购物车中未选中状态
        int CHECKED = 1; // 购物车中已选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum OrderStatus {
        CANCLE(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        OrderStatus(int code, String value) {
            this.value = value;
            this.code = code;
        }

        private String value;
        private int code;

        public void setValue(String value) {
            this.value = value;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static String getDesc(int code) {
            for (OrderStatus orderStatus : values()) {
                if (orderStatus.code == code) {
                    return orderStatus.value;
                }
            }
            throw new RuntimeException("没有对应的支付类型");
        }
    }

    public interface AlipayCallback {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatform {
        ALIPAY(1, "支付宝");
        private String value;
        private int code;

        PayPlatform(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public enum PaymentType {
        ONLINE_PAY(1, "在线支付");

        private String value;
        private int code;

        PaymentType(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static String getDesc(int code) {
            for (PaymentType paymentType : values()) {
                if (paymentType.code == code) {
                    return paymentType.value;
                }
            }
            throw new RuntimeException("没有对应的支付类型");
        }
    }

}
