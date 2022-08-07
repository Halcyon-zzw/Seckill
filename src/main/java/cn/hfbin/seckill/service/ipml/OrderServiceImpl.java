package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.dao.OrdeInfoMapper;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.service.OrderService;
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
