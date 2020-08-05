package org.seckill.controller;

import com.alibaba.druid.util.StringUtils;
import org.seckill.access.AccessLimit;
import org.seckill.bean.User;
import org.seckill.redis.GoodsKey;
import org.seckill.redis.RedisService;
import org.seckill.result.Result;
import org.seckill.service.GoodsService;
import org.seckill.vo.GoodsDetailVo;
import org.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request, HttpServletResponse response,
                         Model model, User user){
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsVos);
        model.addAttribute("user", user);
        //手动渲染
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",context);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String toDetail(HttpServletRequest request, HttpServletResponse response,
                         Model model, User user, @PathVariable("goodsId") long goodsId){
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        GoodsVo goodsVoById = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goodsVoById);
        model.addAttribute("user", user);

        long startTime = goodsVoById.getStartDate().getTime();
        long endTime = goodsVoById.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowTime < startTime){
            seckillStatus = 0;
            remainSeconds = (int) ((startTime - nowTime) / 1000);
        }else if (nowTime > endTime){
            seckillStatus = 2;
            remainSeconds = -1;
        }else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus",seckillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        //手动渲染
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),
                request.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",context);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }

    /**
     * 加载静态页面
     * @param request
     * @param response
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/to_detail1/{goodsId}")
    @ResponseBody
    @AccessLimit(seconds = 30,maxCount = 5)
    public Result<GoodsDetailVo> toDetail1(HttpServletRequest request, HttpServletResponse response,
                                           Model model, User user, @PathVariable("goodsId") long goodsId){
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        long startTime = goods.getStartDate().getTime();
//        logger.error("startTime----"+new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(goods.getStartDate()));
        long endTime = goods.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowTime < startTime){
            seckillStatus = 0;
            remainSeconds = (int) ((startTime - nowTime) / 1000);
        }else if (nowTime > endTime){
            seckillStatus = 2;
            remainSeconds = -1;
        }else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo(seckillStatus,remainSeconds,goods,user);
        return Result.success(goodsDetailVo);
    }
}
