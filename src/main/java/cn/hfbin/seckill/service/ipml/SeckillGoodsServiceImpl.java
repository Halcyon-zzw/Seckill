package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.dao.GoodsMapper;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Date 2022/7/24 17:32
 * @Author zhuzhiwei
 */
@Service("seckillGoodsService")
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    private final GoodsMapper goodsMapper;

    public SeckillGoodsServiceImpl(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
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
        return goodsMapper.updateStock(goodsId);
    }
}
