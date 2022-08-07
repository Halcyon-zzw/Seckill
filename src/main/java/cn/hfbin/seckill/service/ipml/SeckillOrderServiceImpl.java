package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.dao.SeckillOrderMapper;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.exception.SecKillException;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.OrderService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * @Description 秒杀订单serviceImpl
 * @Date 2022/8/5 20:42
 * @Author zhuzhiwei
 */
@Slf4j
@Service("seckillOrderService")
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private final SeckillOrderMapper seckillOrderMapper;

    private final SeckillGoodsService seckillGoodsService;

    private final RedisService redisService;

    private final OrderService orderService;

    public SeckillOrderServiceImpl(SeckillOrderMapper seckillOrderMapper,
                                   SeckillGoodsService seckillGoodsService,
                                   RedisService redisService,
                                   OrderService orderService) {
        this.seckillOrderMapper = seckillOrderMapper;
        this.seckillGoodsService = seckillGoodsService;
        this.redisService = redisService;
        this.orderService = orderService;
    }

    @Override
    public SeckillOrder getSeckillOrderByUserIdGoodsId(long userId, long goodsId) {
        return seckillOrderMapper.selectByUserIdAndGoodsId(userId, goodsId);
    }

    @Transactional
    @Override
    public OrderInfo insert(long userId, GoodsBo goods) {
        //秒杀商品库存减一
        int success = seckillGoodsService.reduceStock(goods.getId());
        if (success == 1) {
            OrderInfo orderInfo = new OrderInfo()
                    .setCreateDate(new Date())
                    .setAddrId(0L)
                    .setGoodsCount(1)
                    .setGoodsId(goods.getId())
                    .setGoodsName(goods.getGoodsName())
                    .setGoodsPrice(goods.getSeckillPrice())
                    .setOrderChannel(1)
                    .setStatus(0)
                    .setUserId(userId);
            //添加信息进订单
            long orderId = orderService.addOrder(orderInfo);
            log.info("orderId -->" + orderId + "");
            SeckillOrder seckillOrder = new SeckillOrder()
                    .setGoodsId(goods.getId())
                    .setOrderId(orderInfo.getId())
                    .setUserId(userId);
            //插入秒杀表
            seckillOrderMapper.insertSelective(seckillOrder);
            return orderInfo;
        } else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    @Override
    public OrderInfo getOrderInfo(long orderId) {
        SeckillOrder seckillOrder = seckillOrderMapper.selectByPrimaryKey(orderId);
        if (seckillOrder == null) {
            throw new SecKillException(CodeMsg.ORDER_NOT_EXIST);
        }
        OrderInfo orderInfo = orderService.getOrderInfo(seckillOrder.getOrderId());
        if (orderInfo == null) {
            throw new SecKillException(CodeMsg.ORDER_NOT_EXIST);
        }
        return orderInfo;
    }

    public long getSeckillResult(Long userId, long goodsId) {
        SeckillOrder order = getSeckillOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            //秒杀成功
            return order.getOrderId();
        }
        if (isGoodsOver(goodsId)) {
            return -1;
        } else {
            return 0;
        }
    }

    /*
     * 秒杀商品结束标记
     * */
    private void setGoodsOver(Long goodsId) {
        redisService.set(RedisPrefixKeyConst.GOODS_OVER, goodsId.toString(), true, Const.RedisCacheExtime.GOODS_ID);
    }

    /*
     * 查看秒杀商品是否已经结束
     * */
    private boolean isGoodsOver(long goodsId) {
        return redisService.exists(RedisPrefixKeyConst.GOODS_OVER, String.valueOf(goodsId));
    }

}
