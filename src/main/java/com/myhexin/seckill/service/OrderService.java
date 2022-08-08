package com.myhexin.seckill.service;

import com.myhexin.seckill.entity.OrderInfo;


/**
 * @Date 2022/7/31 16:09
 * @Author zhuzhiwei
 */
public interface OrderService {

    long addOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfo(long rderId);
}
