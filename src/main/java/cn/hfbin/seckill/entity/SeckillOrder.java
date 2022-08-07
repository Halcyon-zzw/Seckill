package cn.hfbin.seckill.entity;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@ToString
@AllArgsConstructor
@Accessors(chain = true)
public class SeckillOrder {
    private Long id;

    private Long userId;

    private Long orderId;

    private Long goodsId;

    public SeckillOrder() {
    }
}