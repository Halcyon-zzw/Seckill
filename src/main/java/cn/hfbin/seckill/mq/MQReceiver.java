package cn.hfbin.seckill.mq;

import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MQReceiver.class);

    private final SeckillGoodsService goodsService;

    private final SeckillOrderService seckillOrderService;

    public MQReceiver(SeckillGoodsService goodsService,
                      SeckillOrderService seckillOrderService) {
        this.goodsService = goodsService;
        this.seckillOrderService = seckillOrderService;
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message) {
        LOGGER.info("receive message:" + message);
        SeckillMessage mm = RedisService.stringToBean(message, SeckillMessage.class);
        User user = mm.getUser();
        long goodsId = mm.getGoodsId();

        GoodsBo goods = goodsService.getseckillGoodsBoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        seckillOrderService.insert(user, goods);
    }
}
