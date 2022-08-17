package com.myhexin.seckill.controller;

import com.myhexin.seckill.common.Const;
import com.myhexin.seckill.common.RedisPrefixKeyConst;
import com.myhexin.seckill.common.SeckillConst;
import com.myhexin.seckill.entity.SeckillGoods;
import com.myhexin.seckill.entity.dto.ProductDeplouResponse;
import com.myhexin.seckill.entity.dto.SeckillEvent;
import com.myhexin.seckill.entity.dto.SeckillResponse;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
//    @PostConstruct
//    public void cache() {
//        List<SeckillGoods> seckillGoodsList = seckillGoodsService.getSeckillGoodsList();
//        if (seckillGoodsList == null) {
//            return;
//        }
//        seckillGoodsList.forEach(goods -> {
//            Long projectId = goods.getGoodsId();
//            redisService.set(RedisPrefixKeyConst.GOODS_STOCK, projectId.toString(), goods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
//            localOverMap.put(projectId, false);
//        });
//    }
    @PostMapping(value = "/deploy")
    public ProductDeplouResponse seckillDeploy(@RequestParam("product_id") Long productId,
                                               @RequestParam("product_amount") Integer productAmount,
                                               @RequestParam("start_date_time") String startDateTime) {
        checkParam(productId, productAmount, startDateTime);
        final SeckillGoods seckillGoods;
        try {
            seckillGoods = seckillGoodsService.deployProduct(productId, productAmount, startDateTime);
            String projectIdStr = seckillGoods.getGoodsId().toString();
            String eventIdStr = seckillGoods.getId().toString();
            redisService.set(RedisPrefixKeyConst.GOODS_STOCK, projectIdStr, seckillGoods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
            redisService.set(RedisPrefixKeyConst.GOODS_LIST, eventIdStr, seckillGoods, Const.RedisCacheExtime.GOODS_LIST);
            return ProductDeplouResponse.success(new SeckillEvent(seckillGoods.getId()));
        } catch (Exception e) {
            LOGGER.error("部署失败", e);
            return ProductDeplouResponse.fail(SeckillConst.SERVER_ERROR);
        }
    }


    @PostMapping(value = "/seckill")
    public SeckillResponse seckillProduct(@RequestParam("event_id") Long eventId,
                                          @RequestParam("user_id") Long userId) {
        //秒杀商品缓存用不过期，不用考虑过期情况。（可定期删除过期数据）
        String eventIdStr = eventId.toString();

        final SeckillGoods seckillGoods = redisService.get(RedisPrefixKeyConst.GOODS_LIST, eventIdStr, SeckillGoods.class);
        if (seckillGoods == null) {
            return SeckillResponse.fail(SeckillConst.NO_PRODUCT);
        }
        Date startDate = seckillGoods.getStartDate();
        Long productId = seckillGoods.getGoodsId();
        String productIdStr = productId.toString();
        final SeckillResponse seckillResponse = checkSecKill(startDate, productId);
        if (seckillResponse != null) {
            return seckillResponse;
        }
        //预减库存
        long stock = redisService.decr(RedisPrefixKeyConst.GOODS_STOCK, productIdStr);
        if (stock < 0) {
            localOverMap.put(productId, true);
            return SeckillResponse.fail(SeckillConst.SECKILL_OVER);
        }
        //判断是否重复秒杀
        if (redisService.exists(RedisPrefixKeyConst.GOODS_USER_ORDER, userId + "_" + productId)) {
            //重复秒杀，自增
            redisService.incr(RedisPrefixKeyConst.GOODS_STOCK, productIdStr);
            return SeckillResponse.fail(SeckillConst.SECKILL_REPEATE);
        }
        //入队
        mqSender.sendSeckillMessage(new SeckillMessage(userId, productId));
        return SeckillResponse.success();
    }

    private void checkParam(Long productId, Integer productAmount, String startDateTime) {
        try {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateTime);
        } catch (ParseException e) {
            String msg = String.format(SeckillConst.BIND_ERROR, "开始时间", startDateTime);
            throw new SecKillException(msg);
        }
        if (productId < 0) {
            throw new SecKillException("商品id不能为负数");
        }
        if (productAmount <= 0) {
            throw new SecKillException("秒杀数量必须为正数");
        }
    }

    /**
     * 验证商品是否秒杀结束
     *
     * @param startDate 活动开始时间
     * @param 商品id
     */
    private SeckillResponse checkSecKill(Date startDate, long project) {
        Date nowDate = new Date();
        if (nowDate.getTime() < startDate.getTime()) {
            return SeckillResponse.fail(SeckillConst.SECKILL_NOT_START);
        }
        boolean over = localOverMap.getOrDefault(project, false);
        if (over) {
            return SeckillResponse.fail(SeckillConst.SECKILL_OVER);
        }
        return null;
    }


    @GetMapping(value = "/test-seckill")
    public void test02(@RequestParam("product_id") Long productId,
                       @RequestParam("product_amount") Integer productAmount,
                       @RequestParam("user_count") int count) throws InterruptedException {

        final ProductDeplouResponse productDeplouResponse = seckillDeploy(productId, productAmount, "2022-8-8 12:00:00");
        SeckillEvent seckillEvent = (SeckillEvent) productDeplouResponse.getData();
        Map<String, List<Long>> map = new ConcurrentHashMap<>();
        for (int i = 0; i < count; i++) {
            Long userId = (long) i;
            new Thread(() -> {
                final SeckillResponse seckillResponse = seckillProduct(seckillEvent.getEventId(), userId);
                final String statusCode = seckillResponse.getStatusCode();
                if (map.get(statusCode) == null) {
                    map.put(statusCode, new CopyOnWriteArrayList<>());
                }
                map.get(statusCode).add(userId);
            }).start();
        }
        Thread.sleep(5000);
        map.forEach((k, v) -> {
            if ("0".equals(k)) {
                System.out.println("======================");
                System.out.println("抢到商品人数: " + v.size());
                System.out.println("去重后数量: " + new HashSet<>(v).size());
            } else {
                System.out.println("======================");
                System.out.println("未抢到商品人数: " + v.size());
                System.out.println("去重后数量: " + new HashSet<>(v).size());
            }
        });
    }

    private CodeMsg getParamError(String param) {
        return new CodeMsg(500101, "参数有误：" + param);
    }

    public SeckillResponse seckillResult(long userId, long productId) {
        while (true) {
            long result = seckillOrderService.getSeckillResult(userId, productId);
            if (result > 0) {
                return SeckillResponse.success();
            } else if (result < 0) {
                LOGGER.info("===========商品秒杀完=========");
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
}
