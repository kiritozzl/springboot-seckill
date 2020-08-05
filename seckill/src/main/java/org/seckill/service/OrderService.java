package org.seckill.service;

import org.seckill.bean.OrderInfo;
import org.seckill.bean.SeckillOrder;
import org.seckill.bean.User;
import org.seckill.mapper.OrderMapper;
import org.seckill.redis.OrderKey;
import org.seckill.redis.RedisService;
import org.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    RedisService redisService;

    @Autowired
    OrderMapper orderMapper;

    public SeckillOrder getOrderByUserIdGoodsId(long id, long goodsId) {
        return redisService.get(OrderKey.getSeckillOrderByUidGid,""+id+"_"+goodsId,SeckillOrder.class);
    }

    public OrderInfo getOrderById(long orderId){
        return orderMapper.getOrderByGoodsId(orderId);
    }

    public boolean clearOrderData(){
        return orderMapper.clearOrderData() > 0;
    }

    /**
     * 因为要同时分别在订单详情表和秒杀订单表都新增一条数据，所以要保证两个操作是一个事物
     */
    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderMapper.insert(orderInfo);

        SeckillOrder seckillOrder = new SeckillOrder(user.getId(),orderInfo.getId(),goods.getId());
        orderMapper.insertSeckillOrder(seckillOrder);
        redisService.set(OrderKey.getSeckillOrderByUidGid,""+user.getId()+"_"+goods.getId(),seckillOrder);
        return orderInfo;
    }
}
