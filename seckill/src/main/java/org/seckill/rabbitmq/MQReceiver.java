package org.seckill.rabbitmq;

import org.seckill.bean.OrderInfo;
import org.seckill.bean.SeckillOrder;
import org.seckill.bean.User;
import org.seckill.redis.RedisService;
import org.seckill.service.GoodsService;
import org.seckill.service.OrderService;
import org.seckill.service.SeckillService;
import org.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static final Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String msg){
        log.info("receive msg "+msg);

        SeckillMessage seckillMessage = RedisService.stringToBean(msg, SeckillMessage.class);
        User user = seckillMessage.getUser();
        long goodsId = seckillMessage.getGoodsId();

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if (stock <= 0) return;
        //判断重复秒杀
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) return;
        OrderInfo seckill = seckillService.seckill(user, goodsVo);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String msg){
        log.info("topic1 receive "+msg);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String msg){
        log.info("topic2 receive "+msg);
    }
}
