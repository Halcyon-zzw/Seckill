package com.myhexin.seckill.controller;

import com.myhexin.seckill.common.Const;
import com.myhexin.seckill.common.RedisPrefixKeyConst;
import com.myhexin.seckill.common.SeckillConst;
import com.myhexin.seckill.entity.SeckillGoods;
import com.myhexin.seckill.entity.SeckillOrder;
import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.dto.*;
import com.myhexin.seckill.entity.result.CodeMsg;
import com.myhexin.seckill.exception.SecKillException;
import com.myhexin.seckill.exception.SecKillExceptionHandler;
import com.myhexin.seckill.mq.MQSender;
import com.myhexin.seckill.mq.SeckillMessage;
import com.myhexin.seckill.redis.RedisService;
import com.myhexin.seckill.service.SeckillGoodsService;
import com.myhexin.seckill.service.SeckillOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.myhexin.seckill.common.Const.POLL_SLEEP_TIME;

/**
 * @Description 秒杀Controller
 * @Date 2022/8/4 21:42
 * @Author zhuzhiwei
 */
@RestController
@RequestMapping("/product")
public class SeckillController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecKillExceptionHandler.class);

    private final RedisService redisService;
    private final SeckillGoodsService seckillGoodsService;
    private final SeckillOrderService seckillOrderService;
    private final MQSender mqSender;

    /**
     * 本地保存秒杀完的商品，减少redis的访问量
     */
    private final HashMap<Long, Boolean> localOverMap = new HashMap<>(128);

    public SeckillController(RedisService redisService,
                             SeckillGoodsService seckillGoodsService,
                             SeckillOrderService seckillOrderService,
                             MQSender mqSender) {
        this.redisService = redisService;
        this.seckillGoodsService = seckillGoodsService;
        this.seckillOrderService = seckillOrderService;
        this.mqSender = mqSender;
    }

    /**
     * 系统初始化
     */
    @PostConstruct
    public void cache() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.getSeckillGoodsList();
        if (seckillGoodsList == null) {
            return;
        }
        seckillGoodsList.forEach(goods -> {
            Long eventId = goods.getId();
            redisService.set(RedisPrefixKeyConst.GOODS_STOCK, eventId.toString(), goods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
            localOverMap.put(eventId, false);
        });
    }

    @PostMapping(value = "/deploy")
    public ProductDeplouResponse seckillDeploy(@RequestParam("product_id") Long productId,
                                               @RequestParam("product_amount") Integer productAmount,
                                               @RequestParam("start_date_time") String startDateTime) {
        checkParam(productId, productAmount, startDateTime);
        final SeckillGoods seckillGoods;
        try {
            seckillGoods = seckillGoodsService.deployProduct(productId, productAmount, startDateTime);
        } catch (Exception e) {
            return ProductDeplouResponse.fail(SeckillConst.SERVER_ERROR);
        }
        String eventIdStr = seckillGoods.getId().toString();
        redisService.set(RedisPrefixKeyConst.GOODS_STOCK, eventIdStr, seckillGoods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
        return ProductDeplouResponse.success(new SeckillEvent(seckillGoods.getId()));
    }


    @PostMapping(value = "/seckill")
    public SeckillResponse seckillProduct(@RequestParam("event_id") Long eventId, @RequestParam("user_id") Long userId) {
        //秒杀商品缓存用不过期，不用考虑过期情况。（可定期删除过期数据）
        String eventIdStr = eventId.toString();
        if (redisService.get(RedisPrefixKeyConst.GOODS_STOCK, eventIdStr) == null) {
            throw new SecKillException(SeckillConst.NO_PRODUCT);
        }
        SeckillGoods seckillGoods = seckillGoodsService.getByEventId(eventId);
        Date startDate = seckillGoods.getStartDate();
        Long productId = seckillGoods.getGoodsId();
        checkSecKill(startDate, productId);

        //预减库存
        long stock = redisService.decr(RedisPrefixKeyConst.GOODS_STOCK, eventIdStr);
        if (stock < 0) {
            localOverMap.put(userId, true);
            return SeckillResponse.fail(SeckillConst.SECKILL_OVER);
        }
        //判断是否重复秒杀
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(userId, productId);
        if (order != null) {
            redisService.incr(RedisPrefixKeyConst.GOODS_STOCK, eventIdStr);
            localOverMap.put(userId, false);
            return SeckillResponse.fail(SeckillConst.SECKILL_REPEATE);
        }
        //入队
        mqSender.sendSeckillMessage(new SeckillMessage(userId, productId));
        return seckillResult(userId, productId);
    }

    private void checkParam(Long productId, Integer productAmount, String startDateTime) {
        try {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateTime);
        } catch (ParseException e) {
            String msg = String.format(SeckillConst.BIND_ERROR, "开始时间", startDateTime);
            throw  new SecKillException(msg);
        }
        if (productId < 0) {
            throw  new SecKillException("商品id不能为负数");
        }
        if (productAmount <= 0) {
            throw  new SecKillException("秒杀数量必须为正数");
        }
    }

    /**
     * 验证商品是否秒杀结束
     *
     * @param startDate 活动开始时间
     * @param goodsId 商品id
     */
    private void checkSecKill(Date startDate, long goodsId) {
        Date nowDate = new Date();
        if (nowDate.getTime() < startDate.getTime()) {
            throw new SecKillException(SeckillConst.SECKILL_NOT_START);
        }
        boolean over = localOverMap.getOrDefault(goodsId, false);
        if (over) {
            throw new SecKillException(SeckillConst.SECKILL_OVER);
        }
    }


    public SeckillResponse seckillResult(long userId, long productId) {
        while (true) {
            long result = seckillOrderService.getSeckillResult(userId, productId);
            if (result > 0) {
                return SeckillResponse.success();
            } else if (result < 0) {
                return SeckillResponse.fail(SeckillConst.SECKILL_OVER);
            } else {
                try {
                    Thread.sleep(POLL_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }


    private CodeMsg getParamError(String param) {
        return new CodeMsg(500101, "参数有误：" + param);
    }
}
