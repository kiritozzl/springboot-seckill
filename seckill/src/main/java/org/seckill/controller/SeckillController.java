package org.seckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import org.seckill.access.AccessLimit;
import org.seckill.bean.OrderInfo;
import org.seckill.bean.SeckillOrder;
import org.seckill.bean.User;
import org.seckill.rabbitmq.MQSender;
import org.seckill.rabbitmq.SeckillMessage;
import org.seckill.redis.*;
import org.seckill.result.CodeMsg;
import org.seckill.result.Result;
import org.seckill.service.GoodsService;
import org.seckill.service.OrderService;
import org.seckill.service.SeckillService;
import org.seckill.util.MD5Util;
import org.seckill.util.UUIDUtil;
import org.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SeckillController.class);

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    MQSender sender;

    @Autowired
    SeckillService seckillService;

    //基于令牌桶算法的限流实现类
    RateLimiter rateLimiter = RateLimiter.create(10);

    //做标记，判断该商品是否被处理过了
    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    @RequestMapping(value = "/do_seckill1",method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> list1(Model model, User user, @RequestParam("goodsId") long goodsId){
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getGoodsStock();
        if (stock <= 0){
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
//        seckillService.testUpdate();

        OrderInfo seckill = seckillService.seckill(user, goods);
        model.addAttribute("orderInfo",seckill);
        model.addAttribute("goods",goods);
        return Result.success(seckill);
    }

    @RequestMapping(value = "/{path}/do_seckill",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, User user, @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path)throws Exception {
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
            return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
        }
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) return Result.error(CodeMsg.SECKILL_PATH_WRONG);
        model.addAttribute("user",user);
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over){
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getGoodsStock,""+goodsId);
        if (stock < 0){
            afterPropertiesSet();
            long stock2 = redisService.decr(GoodsKey.getGoodsStock,""+goodsId);
            if (stock2 < 0){
                localOverMap.put(goodsId,true);
                return Result.error(CodeMsg.SECKILL_OVER);
            }
        }
        //判断重复秒杀
//        SeckillOrder seckillOrder = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
//        if (seckillOrder != null){
//            return Result.error(CodeMsg.REPEATE_SECKILL);
//        }
        SeckillMessage seckillMessage = new SeckillMessage(user,goodsId);
        sender.sendSeckillMessage(seckillMessage);
        return Result.success(0);//排队中
    }

    /**
     * 系统初始化,将商品信息加载到redis和本地内存
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        if (goodsVos == null) return;
        for (GoodsVo goodsVo:goodsVos){
            redisService.set(GoodsKey.getGoodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(),false);
        }
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, User user, @RequestParam("goodsId") long goodsId){
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);
        long orderId = seckillService.getSeckillResult(user.getId(),goodsId);
        return Result.success(orderId);
    }

    /**
     * 重置数据
     * @param model
     * @return
     */
    @RequestMapping(value = "/reset",method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model){
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods:goodsList){
            goods.setStockCount(10);
            localOverMap.put(goods.getId(),false);
        }
        return Result.success(orderService.clearOrderData());
    }

    /**
     * 添加path隐藏实际秒杀链接
     *
     * @param user
     * @param goodsId
     * @return
     * @throws Exception
     */
    @AccessLimit(seconds = 30,maxCount = 5)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getPath(HttpServletRequest request, User user, @RequestParam("goodsId") long goodsId,
                                  @RequestParam(value = "verifyCode") int verifyCode)throws Exception {
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        if (!seckillService.checkVerifycode(verifyCode, user, goodsId)) return Result.error(CodeMsg.SECKILL_VERIFYCODE_ERROR);
        String str = seckillService.createPath(user,goodsId);
        return Result.success(str);//排队中
    }

    /**
     * 生成验证码，校验
     * @param response
     * @param user
     * @param goodsId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, User user,
                                               @RequestParam("goodsId") long goodsId)throws Exception {
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = seckillService.createVerifyCode(user,goodsId);
        try{
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"JPEG",os);
            os.flush();
            os.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_ERROR);
        }
    }
}
