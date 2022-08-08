package com.myhexin.seckill.controller;

import com.myhexin.seckill.entity.OrderInfo;
import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.vo.OrderDetailVo;
import com.myhexin.seckill.service.SeckillGoodsService;
import com.myhexin.seckill.service.SeckillOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Date 2022/7/24 16:58
 * @Author zhuzhiwei
 */
@Controller
@RequestMapping("/order")
public class SeckillOrderController {
    private final SeckillOrderService seckillOrderService;
    private final SeckillGoodsService seckillGoodsService;

    public SeckillOrderController(SeckillOrderService seckillOrderService,
                                  SeckillGoodsService seckillGoodsService) {
        this.seckillOrderService = seckillOrderService;
        this.seckillGoodsService = seckillGoodsService;
    }

    @RequestMapping("/detail")
    @ResponseBody
    public OrderDetailVo info(@RequestParam("orderId") long orderId) {
        OrderInfo order = seckillOrderService.getOrderInfo(orderId);
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(order.getGoodsId());
        OrderDetailVo vo = new OrderDetailVo(goods, order);
        return vo;
    }
}
