package com.myhexin.seckill.config;

import com.myhexin.seckill.mq.MQConfig;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue queue() {
        return new Queue(MQConfig.MIAOSHA_QUEUE);
    }
}
