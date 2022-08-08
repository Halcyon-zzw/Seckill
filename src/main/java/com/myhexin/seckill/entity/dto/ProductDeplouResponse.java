package com.myhexin.seckill.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description 秒杀商品部署Response
 * @Date 2022/8/6 21:38
 * @Author zhuzhiwei
 */
@Data
public class ProductDeplouResponse<T> {
    private static final String SECKILL_SUCCESS = "0";
    private static final String SECKILL_FAIL = "-1";

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("status_msg")
    private String statusMsg;

    private T data;

    public ProductDeplouResponse(String statusCode, String statusMsg) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
    }

    public ProductDeplouResponse(String statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.statusMsg = "";
    }

    public static <T> ProductDeplouResponse success(T data) {
        return new ProductDeplouResponse(SECKILL_SUCCESS, data);
    }

    public static ProductDeplouResponse fail(String msg) {
        return new ProductDeplouResponse(SECKILL_FAIL, msg);
    }
}
