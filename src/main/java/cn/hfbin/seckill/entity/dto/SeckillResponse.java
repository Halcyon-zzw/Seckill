package cn.hfbin.seckill.entity.dto;

import lombok.Data;

/**
 * @Description 秒杀结果
 * @Date 2022/8/6 21:39
 * @Author zhuzhiwei
 */
@Data
public class SeckillResponse {

    private static final String SECKILL_SUCCESS = "0";
    private static final String SECKILL_FAIL = "-1";

    private String statusCode;

    private String statusMsg;

    public SeckillResponse(String statusCode, String statusMsg) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
    }

    public static SeckillResponse success() {
        return new SeckillResponse(SECKILL_SUCCESS, "");
    }

    public static SeckillResponse fail(String msg) {
        return new SeckillResponse(SECKILL_FAIL, msg);
    }
}
