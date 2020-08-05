package org.seckill.controller;

import org.seckill.dao.Seckill;
import org.seckill.rabbitmq.MQSender;
import org.seckill.redis.BasePrefix;
import org.seckill.redis.GoodsKey;
import org.seckill.redis.RedisService;
import org.seckill.result.CodeMsg;
import org.seckill.result.Result;
import org.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisService redisService;

    @Autowired
    MQSender sender;

    @RequestMapping("/")
    @ResponseBody
    String home(){
        return "hello world";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello(){
        return Result.success("hello");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> error(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(){
        return "hello";
    }

//    @RequestMapping("/db/get")
//    @ResponseBody
//    public Result<Seckill> dbGet(){
//        return Result.success(seckillService.getBtId(1001L));
//    }
//
//    @RequestMapping("/db/tx")
//    @ResponseBody
//    public boolean dbtx(){
//        return seckillService.transact();
//    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public boolean redisSet(){
        return redisService.set(GoodsKey.getGoodsDetail,"second","asuna");
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<String> redisGet(){
        return Result.success(redisService.get(GoodsKey.getGoodsDetail,"second",String.class));
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> send(){
        sender.sendTopic("hello asuna");
        return Result.success("hello");
    }
}
