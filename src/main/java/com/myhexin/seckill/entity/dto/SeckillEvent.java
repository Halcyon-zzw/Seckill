package com.myhexin.seckill.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description TODO
 * @Date 2022/8/7 18:57
 * @Author zhuzhiwei
 */
@Data
@AllArgsConstructor
public class SeckillEvent {
    @JsonProperty("event_id")
    private Long eventId;
}
