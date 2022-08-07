package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.annotations.AccessLimit;
import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.exception.SecKillException;
import cn.hfbin.seckill.mq.MQSender;
import cn.hfbin.seckill.mq.SeckillMessage;
import cn.hfbin.seckill.redis.GoodsKey;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * @Description 秒杀Controller
 * @Date 2022/8/4 21:42
 * @Author zhuzhiwei
 */
@RestController
@RequestMapping("seckill")
public class SeckillController {

    private final UserService userService;
    private final RedisService redisService;

    private final SeckillGoodsService seckillGoodsService;

    private final SeckillOrderService seckillOrderService;

    private final MQSender mqSender;

    /**
     * 本地保存秒杀完的商品，减少redis的访问量
     */
    private final HashMap<Long, Boolean> localOverMap = new HashMap<>(128);

    public SeckillController(UserService userService,
                             RedisService redisService,
                             SeckillGoodsService seckillGoodsService,
                             SeckillOrderService seckillOrderService,
                             MQSender mqSender) {
        this.userService = userService;
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


    @PostMapping(value = "/{path}/seckill")
    public Result<Integer> list(@RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path,
                                HttpServletRequest request) {
        User user = userService.getUserByRequest(request);
        checkSecKill(goodsId, path, user);
        //预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, String.valueOf(goodsId));
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        mqSender.sendSeckillMessage(new SeckillMessage(user, goodsId));
        return Result.success(0);
    }

    /**
     * 验证链接及商品是否秒杀结束
     *
     * @param goodsId 商品id
     * @param path    商品链接
     * @param user    用户
     */
    private void checkSecKill(long goodsId, String path, User user) {
        //验证path
        boolean check = seckillOrderService.checkPath(user, goodsId, path);
        if (!check) {
            throw new SecKillException(CodeMsg.REQUEST_ILLEGAL);
        }
        boolean over = localOverMap.getOrDefault(goodsId, false);
        if (over) {
            throw new SecKillException(CodeMsg.MIAO_SHA_OVER);
        }
    }

    /**
     * 客户端轮询查询是否下单成功
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @GetMapping(value = "/result")
    @ResponseBody
    public Result<Long> miaoshaResult(@RequestParam("goodsId") long goodsId, HttpServletRequest request) {
        User user = userService.getUserByRequest(request);
        long result = seckillOrderService.getSeckillResult((long) user.getId(), goodsId);
        return Result.success(result);
    }


    /**
     * 动态生成秒杀的链接
     *
     * @param request request
     * @param goodsId 商品id
     * @return 动态链接
     */
    @AccessLimit(seconds = 5, maxCount = 5)
    @GetMapping(value = "/path")
    public Result<String> getSecKillPath(HttpServletRequest request, @RequestParam("goodsId") long goodsId) {
        User user = userService.getUserByRequest(request);
        String path = seckillOrderService.createSecKillPath(user, goodsId);
        return Result.success(path);
    }
}
