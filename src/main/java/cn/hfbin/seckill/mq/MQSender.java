package cn.hfbin.seckill.mq;

import cn.hfbin.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;


@Service
public class MQSender {

    private static final Logger log = LoggerFactory.getLogger(MQSender.class);

    final
    AmqpTemplate amqpTemplate;

    public MQSender(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendSeckillMessage(SeckillMessage mm) {
        String msg = RedisService.beanToString(mm);
        log.info("send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
    }

}
