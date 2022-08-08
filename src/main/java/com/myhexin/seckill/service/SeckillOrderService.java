package com.myhexin.seckill.service;

import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.OrderInfo;
import com.myhexin.seckill.entity.SeckillOrder;

/**
 * @Description 秒杀订单service
 * @Date 2022/8/5 20:42
 * @Author zhuzhiwei
 */
public interface SeckillOrderService {

    SeckillOrder getSeckillOrderByUserIdGoodsId(long userId , long goodsId);


    OrderInfo insert(long userId, GoodsBo goodsBo);

    OrderInfo getOrderInfo(long orderId);

    long getSeckillResult(Long userId, long goodsId);
}
