package com.myhexin.seckill.dao;

import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.Goods;

import java.util.List;

public interface GoodsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Goods record);

    int updateByPrimaryKeyWithBLOBs(Goods record);

    int updateByPrimaryKey(Goods record);

    List<GoodsBo> selectAllGoodes();

    GoodsBo getseckillGoodsBoByGoodsId(long goodsId);

    int decrStock(long goodsId);
}