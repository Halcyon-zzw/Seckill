package com.myhexin.seckill.service.ipml;

import com.myhexin.seckill.dao.GoodsMapper;
import com.myhexin.seckill.dao.SeckillGoodsMapper;
import com.myhexin.seckill.entity.Goods;
import com.myhexin.seckill.entity.SeckillGoods;
import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.exception.SecKillException;
import com.myhexin.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Date 2022/7/24 17:32
 * @Author zhuzhiwei
 */
@Service("seckillGoodsService")
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    private static final String DEFAULT = "default";

    private final GoodsMapper goodsMapper;
    private final SeckillGoodsMapper seckillGoodsMapper;

    public SeckillGoodsServiceImpl(GoodsMapper goodsMapper,
                                   SeckillGoodsMapper seckillGoodsMapper) {
        this.goodsMapper = goodsMapper;
        this.seckillGoodsMapper = seckillGoodsMapper;
    }

    @Override
    public List<GoodsBo> getSeckillGoodsList() {
        return goodsMapper.selectAllGoodes();
    }

    @Override
    public GoodsBo getseckillGoodsBoByGoodsId(long goodsId) {
        return goodsMapper.getseckillGoodsBoByGoodsId(goodsId);
    }

    @Override
    public int reduceStock(long goodsId) {
        return goodsMapper.decrStock(goodsId);
    }

    @Override
    public SeckillGoods deployProduct(Long productId, Integer productAmount, String startDateTime) {

        final Goods alreadyGoods = goodsMapper.selectByPrimaryKey(productId);
        if (alreadyGoods == null) {
            Goods goods = new Goods()
                    .setId(productId)
                    .setGoodsStock(productAmount + 10000)
                    .setGoodsImg(DEFAULT)
                    .setGoodsDetail(DEFAULT)
                    .setGoodsName(DEFAULT)
                    .setGoodsPrice(new BigDecimal(0.0))
                    .setGoodsTitle(DEFAULT)
                    .setCreateDate(new Date())
                    .setUpdateDate(new Date());
            goodsMapper.insertSelective(goods);
        }
        Date startDate;
        Date endDate;
        try {
            startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateTime);
            endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 12:00:00");
        } catch (ParseException e) {
            throw new SecKillException("开始时间有误：" + startDateTime);
        }
        SeckillGoods seckillGoods = new SeckillGoods()
                .setGoodsId(productId)
                .setStockCount(productAmount)
                .setSeckilPrice(new BigDecimal(0.0))
                .setStartDate(startDate)
                .setEndDate(endDate);

        final SeckillGoods alreadySeckillGoods = seckillGoodsMapper.selectByGoodsId(productId);
        if (alreadySeckillGoods != null) {
            seckillGoods.setId(alreadySeckillGoods.getId());
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        } else {
            seckillGoodsMapper.insertSelective(seckillGoods);
        }
        return seckillGoods;
    }

    @Override
    public SeckillGoods getByEventId(Long eventId) {
        SeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(eventId);
        if (seckillGoods == null) {
            throw new SecKillException("没有该秒杀活动:" + eventId);
        }
        return seckillGoods;
    }
}
