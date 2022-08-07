package cn.hfbin.seckill.entity.vo;

import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.User;
import lombok.Data;

/**
 * @Date 2022/8/3 22:19
 * @Author zhuzhiwei
 */
@Data
public class GoodsDetailVo {
    private int miaoshaStatus = 0;
    private int remainSeconds = 0;
    private GoodsBo goods ;
    private User user;
}
