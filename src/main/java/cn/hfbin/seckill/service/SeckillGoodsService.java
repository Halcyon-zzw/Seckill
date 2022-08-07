package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.bo.GoodsBo;

import java.util.List;


/**
 * @Date 2022/7/24 17:23
 * @Author zhuzhiwei
 */
public interface SeckillGoodsService {

    List<GoodsBo> getSeckillGoodsList();

    GoodsBo getseckillGoodsBoByGoodsId(long goodsId);

    int reduceStock(long goodsId);
}
