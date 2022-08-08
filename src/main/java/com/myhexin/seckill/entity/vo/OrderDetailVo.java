package com.myhexin.seckill.entity.vo;

import com.myhexin.seckill.entity.bo.GoodsBo;
import com.myhexin.seckill.entity.OrderInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Date 2022/8/3 22:19
 * @Author zhuzhiwei
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class OrderDetailVo {
    private GoodsBo goods;
    private OrderInfo order;

    public OrderDetailVo(GoodsBo goods, OrderInfo order) {
        this.goods = goods;
        this.order = order;
    }
}
