package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.dao.GoodsMapper;
import cn.hfbin.seckill.dao.SeckillGoodsMapper;
import cn.hfbin.seckill.entity.Goods;
import cn.hfbin.seckill.entity.SeckillGoods;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.dto.ProductDeployRequest;
import cn.hfbin.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public SeckillGoods deployProduct(ProductDeployRequest productDeployRequest) {
        Goods goods = new Goods()
                .setId(productDeployRequest.getProductId())
                .setGoodsStock(productDeployRequest.getProductAmount() + 10000)
                .setGoodsImg(DEFAULT)
                .setGoodsDetail(DEFAULT)
                .setGoodsName(DEFAULT)
                .setGoodsPrice(new BigDecimal(0.0))
                .setGoodsTitle(DEFAULT)
                .setCreateDate(new Date())
                .setUpdateDate(new Date());

        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(productDeployRequest.getStartDateTime());
        Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 12:00:00");
        SeckillGoods seckillGoods = new SeckillGoods()
                .setGoodsId(productDeployRequest.getProductId())
                .setStockCount(productDeployRequest.getProductAmount())
                .setSeckilPrice(new BigDecimal(0.0))
                .setStartDate(startDate)
                .setEndDate(endDate);
        goodsMapper.insertSelective(goods);
        seckillGoodsMapper.insertSelective(seckillGoods);

        return seckillGoods;
    }
}
