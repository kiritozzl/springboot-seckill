package org.seckill.controller;

import org.seckill.bean.OrderInfo;
import org.seckill.bean.User;
import org.seckill.result.CodeMsg;
import org.seckill.result.Result;
import org.seckill.service.GoodsService;
import org.seckill.service.OrderService;
import org.seckill.vo.GoodsVo;
import org.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, User user, @RequestParam("orderId")long orderId){
        if (user == null) return Result.error(CodeMsg.SESSION_ERROR);
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null) return Result.error(CodeMsg.ORDER_NOT_EXIST);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(orderInfo.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo(goodsVo, orderInfo);

        return Result.success(orderDetailVo);
    }
}
