package com.myhexin.seckill.service.ipml;

import com.myhexin.seckill.dao.OrdeInfoMapper;
import com.myhexin.seckill.entity.OrderInfo;
import com.myhexin.seckill.service.OrderService;
import org.springframework.stereotype.Service;

/**
 * @Date 2022/7/24 17:30
 * @Author zhuzhiwei
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {
    private final OrdeInfoMapper ordeInfoMapper;

    public OrderServiceImpl(OrdeInfoMapper ordeInfoMapper) {
        this.ordeInfoMapper = ordeInfoMapper;
    }

    @Override
    public long addOrder(OrderInfo orderInfo) {
        return ordeInfoMapper.insertSelective(orderInfo);
    }

    @Override
    public OrderInfo getOrderInfo(long orderId) {
        return ordeInfoMapper.selectByPrimaryKey(orderId);
    }
}
