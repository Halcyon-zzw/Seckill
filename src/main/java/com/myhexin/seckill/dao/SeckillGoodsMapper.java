package com.myhexin.seckill.dao;

import com.myhexin.seckill.entity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeckillGoods record);

    int insertSelective(SeckillGoods record);

    SeckillGoods selectByPrimaryKey(Long id);

    SeckillGoods selectByGoodsId(Long id);

    int updateByPrimaryKeySelective(SeckillGoods record);

    int updateByPrimaryKey(SeckillGoods record);

    List<SeckillGoods> selectAllSeckillGoods();
}