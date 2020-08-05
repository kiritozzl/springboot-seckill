package org.seckill.rabbitmq;

import org.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate template;

    public void sendTopic(Object msg){
        String ms = RedisService.beanToString(msg);
        log.info("send topic message :"+ms);
        template.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key1",msg+"1");
        template.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",msg+"2");
    }

    public void sendSeckillMessage(SeckillMessage message){
        String ms = RedisService.beanToString(message);
        log.info("send topic message :"+ms);
        template.convertAndSend(MQConfig.QUEUE,ms);
    }
}
