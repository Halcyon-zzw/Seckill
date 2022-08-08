package com.myhexin.seckill.service;

import com.myhexin.seckill.entity.SeckillGoods;
import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.dto.ProductDeployRequest;

import java.util.List;


/**
 * @Date 2022/7/24 17:23
 * @Author zhuzhiwei
 */
public interface SeckillGoodsService {

    List<SeckillGoods> getSeckillGoodsList();

    GoodsBo getseckillGoodsBoByGoodsId(long goodsId);

    int reduceStock(long goodsId);

    SeckillGoods deployProduct(Long productId, Integer productAmount, String startDateTime);

    SeckillGoods getByEventId(Long eventId);
}
