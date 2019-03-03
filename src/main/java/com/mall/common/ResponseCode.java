package com.mall.common;

/**
 * @author yusiming
 * @date 2018/11/22 21:49
 */
public enum ResponseCode {
    SUCCESS(0, "SUCCESS"), ERROR(1, "ERROR"), ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT"), NEED_LOGIN(10, "NEED_LOGIN");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
