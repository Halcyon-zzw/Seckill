package com.myhexin.seckill.common;

/**
 * @Description 常量类
 * @Date 2022/8/7 12:17
 * @Author zhuzhiwei
 */
public class Const {

    public interface RedisCacheExtime{
        /**
         * 30分钟
         */
        int REDIS_SESSION_EXTIME = 60 * 30;
        /**
         * 12小时
         */
        int GOODS_LIST = 60 * 60 * 12;
        /**
         * 1分钟
         */
        int GOODS_ID = 60;
        int SECKILL_PATH = 60;
        int GOODS_INFO = 60;
    }

    /**
     * 获取秒杀结果轮询的间隔
     */
    public static final long POLL_SLEEP_TIME = 500;
    /**
     * 轮询持续的时间
     */
    public static final long POLL_CONTINUE_TIME = 1000 * 60 * 5;
}
