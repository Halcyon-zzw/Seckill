package cn.hfbin.seckill.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description 秒杀商品部署Request
 * @Date 2022/8/6 21:24
 * @Author zhuzhiwei
 */
@Data
public class ProductDeployRequest {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_amount")
    private Integer productAmount;

    @JsonProperty("start_date_time")
    private String startDateTime;
}
