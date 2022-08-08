package com.myhexin.seckill.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description 秒杀请求对象
 * @Date 2022/8/6 21:22
 * @Author zhuzhiwei
 */
@Data
public class SeckillRequest {
    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("user_id")
    private Long userId;
}
