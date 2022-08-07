package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.SeckillGoods;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.dto.ProductDeployRequest;

import java.util.List;


/**
 * @Date 2022/7/24 17:23
 * @Author zhuzhiwei
 */
public interface SeckillGoodsService {

    List<GoodsBo> getSeckillGoodsList();

    GoodsBo getseckillGoodsBoByGoodsId(long goodsId);

    int reduceStock(long goodsId);

    SeckillGoods deployProduct(ProductDeployRequest productDeployRequest);
}
