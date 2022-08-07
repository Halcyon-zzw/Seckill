package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;

/**
 * @Description 秒杀订单service
 * @Date 2022/8/5 20:42
 * @Author zhuzhiwei
 */
public interface SeckillOrderService {

    SeckillOrder getSeckillOrderByUserIdGoodsId(long userId , long goodsId);


    OrderInfo insert(User user , GoodsBo goodsBo);

    OrderInfo getOrderInfo(long orderId);

    long getSeckillResult(Long userId, long goodsId);

    boolean checkPath(User user, long goodsId, String path);

    String createSecKillPath(User user, long goodsId);

}
