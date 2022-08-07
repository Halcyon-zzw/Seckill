package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.common.SeckillConst;
import cn.hfbin.seckill.entity.Goods;
import cn.hfbin.seckill.entity.SeckillGoods;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.dto.ProductDeplouResponse;
import cn.hfbin.seckill.entity.dto.ProductDeployRequest;
import cn.hfbin.seckill.entity.dto.SeckillRequest;
import cn.hfbin.seckill.entity.dto.SeckillResponse;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.exception.SecKillException;
import cn.hfbin.seckill.exception.SecKillExceptionHandler;
import cn.hfbin.seckill.mq.MQSender;
import cn.hfbin.seckill.mq.SeckillMessage;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static cn.hfbin.seckill.common.Const.POLL_SLEEP_TIME;

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
        List<GoodsBo> goodsList = seckillGoodsService.getSeckillGoodsList();
        if (goodsList == null) {
            return;
        }
        goodsList.forEach(goods -> {
            redisService.set(RedisPrefixKeyConst.GOODS_STOCK, goods.getId().toString(), goods.getStockCount(), Const.RedisCacheExtime.GOODS_LIST);
            localOverMap.put(goods.getId(), false);
        });
    }

    @PostMapping(value = "/deploy")
    public ProductDeplouResponse seckillDeploy(@RequestBody ProductDeployRequest productDeployRequest) {
        final ProductDeplouResponse productDeplouResponse = checkParam(productDeployRequest);
        if (productDeplouResponse != null) {
            return productDeplouResponse;
        }
        final SeckillGoods seckillGoods;
        try {
            seckillGoods = seckillGoodsService.deployProduct(productDeployRequest);
        } catch (Exception e) {
            return ProductDeplouResponse.fail(SeckillConst.SERVER_ERROR);
        }
        return ProductDeplouResponse.success(seckillGoods.getId());
    }

    private ProductDeplouResponse checkParam(ProductDeployRequest productDeployRequest) {
        Long productId = productDeployRequest.getProductId();
        Integer productAmount = productDeployRequest.getProductAmount();
        final String startDateTime = productDeployRequest.getStartDateTime();
        try {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateTime);
        } catch (ParseException e) {
            return ProductDeplouResponse.fail("开始时间参数格式有误");
        }
        if (productId < 0) {
            return ProductDeplouResponse.fail("商品id不能为负数");
        }
        if (productAmount <= 0) {
            return ProductDeplouResponse.fail("秒杀数量必须为正数");
        }
        return null;
    }


    @PostMapping(value = "/seckill")
    public SeckillResponse seckillProduct(@RequestBody SeckillRequest seckillRequest) {
        Long userId = seckillRequest.getUserId();
        //TODO 转productId
        Long productId = seckillRequest.getEventId();
        String msg = checkSecKill(productId);
        if (StringUtils.isNotEmpty(msg)) {
            return SeckillResponse.fail(msg);
        }
        //预减库存
        long stock = redisService.decr(RedisPrefixKeyConst.GOODS_STOCK, productId.toString());
        if (stock < 0) {
            localOverMap.put(userId, true);
            return SeckillResponse.fail(SeckillConst.SECKILL_OVER);
        }
        //判断是否重复秒杀
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(userId, productId);
        if (order != null) {
            return SeckillResponse.fail(SeckillConst.SECKILL_REPEATE);
        }
        //入队
        mqSender.sendSeckillMessage(new SeckillMessage(userId, productId));
        return seckillResult(userId, productId);
    }


    /**
     * 验证商品是否秒杀结束
     *
     * @param goodsId 商品id
     */
    private String checkSecKill(long goodsId) {
        //TODO 加入布隆过滤器
        if (goodsId < 0) {
            return SeckillConst.NO_PRODUCT;
        }
        boolean over = localOverMap.getOrDefault(goodsId, false);
        if (over) {
            return SeckillConst.SECKILL_OVER;
        }
        return null;
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
