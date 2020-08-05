package org.seckill.service;

import org.seckill.bean.OrderInfo;
import org.seckill.bean.SeckillOrder;
import org.seckill.bean.User;
import org.seckill.controller.SeckillController;
import org.seckill.dao.Seckill;
import org.seckill.dao.SeckillDao;
import org.seckill.mapper.GoodsMapper;
import org.seckill.redis.RedisService;
import org.seckill.redis.SeckillKey;
import org.seckill.util.MD5Util;
import org.seckill.util.UUIDUtil;
import org.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillService {
    private static final Logger logger = LoggerFactory.getLogger(SeckillService.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsMapper goodsMapper;

    //保证这三个操作，减库存 下订单 写入秒杀订单是一个事物
    @Transactional
    public OrderInfo seckill(User user, GoodsVo goods){
        //减库存
        boolean success = goodsService.reduceStock(goods);
        if (success){
            //下订单 写入秒杀订单
            return orderService.createOrder(user,goods);
        }else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    public boolean testUpdate(){
        int res = goodsMapper.updateStock(); //更新成功返回1
        return res > 0;
    }

    public long getSeckillResult(long userId, long goodsId){
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(userId, goodsId);
        if (order != null){
            return order.getOrderId();
        }else {
            boolean over = getGoodsOver(goodsId);
            if (over){
                return -1;
            }else {
                return 0;
            }
        }
    }

    private void setGoodsOver(Long id) {
        redisService.set(SeckillKey.isGoodsOver,""+id,true);
    }

    public boolean getGoodsOver(Long orderId){
        return redisService.exists(SeckillKey.isGoodsOver,""+orderId);
    }

    public boolean checkPath(User user, long goodsId, String path) {
        if (user == null || path == null) return false;
        String sp = redisService.get(SeckillKey.getSeckillPath,""+user.getId()+"_"+goodsId,String.class);
        return sp.equals(path);
    }

    public String createPath(User user, long goodsId){
        if (user == null || goodsId < 0) return null;
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillKey.getSeckillPath,""+user.getId()+"_"+goodsId,str);
        return str;
    }

    public BufferedImage createVerifyCode(User user, long goodsId) {
        if (user == null || goodsId < 0) return null;
        int width = 80;
        int height = 32;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0,0,width,height);
        g.setColor(Color.BLACK);
        g.drawRect(0,0,width-1,height-1);
        Random rdm = new Random();
        for (int i = 0; i < 50; i++){
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x,y,0,0);
        }
        String verifyCode = createVerifyCode(rdm);
        g.setColor(new Color(0,100,0));
        g.setFont(new Font("Candara", Font.BOLD,24));
        g.drawString(verifyCode, 8,24);
        g.dispose();
        int rnd = calc(verifyCode);
        redisService.set(SeckillKey.getSeckillVerifyCode,""+user.getId()+"_"+goodsId,rnd);

        return image;
    }

    public boolean checkVerifycode(int code, User user, long goodsId){
        if (user == null || goodsId < 0) return false;
        int sc = redisService.get(SeckillKey.getSeckillVerifyCode,""+user.getId()+"_"+goodsId,Integer.class);
        if(sc != code){
            return false;
        }
        redisService.delete(SeckillKey.getSeckillVerifyCode,""+user.getId()+"_"+goodsId);
        return true;
    }

    @SuppressWarnings("restriction")
    private int calc(String code) {
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (int) engine.eval(code);
        } catch (ScriptException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[]{'+','-','*'};

    private String createVerifyCode(Random rdm) {
        int a = rdm.nextInt(10);
        int b = rdm.nextInt(10);
        int c = rdm.nextInt(10);

        return "" + a + ops[rdm.nextInt(3)] + b + ops[rdm.nextInt(3)] + c;
    }
}
