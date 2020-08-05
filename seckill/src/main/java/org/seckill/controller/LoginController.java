package org.seckill.controller;

import org.seckill.result.CodeMsg;
import org.seckill.result.Result;
import org.seckill.service.UserService;
import org.seckill.util.ValidatorUtil;
import org.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        logger.info(loginVo.toString());
//        if (!ValidatorUtil.isMobile(loginVo.getMobile())) return CodeMsg.MOBILE_ERROR;
        String token = userService.login(response, loginVo);
        return Result.success(token);
    }
}
