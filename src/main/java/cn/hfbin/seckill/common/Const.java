package cn.hfbin.seckill.common;

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
}
