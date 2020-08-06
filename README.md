# Java实战电商秒杀系统

本系统是基于SpringBoot开发的电商秒杀系统，实现了用户登录，商品列表，商品详情页，商品秒杀，商品订单等功能，
系统还针对高并发的情况，进行了浏览器缓存，对象缓存，页面缓存，RabbitMQ队列异步下单来减少网络流量，全面提升系统并发能力，
在系统安全方面，应用了图形验证码，限流防刷，接口地址隐藏等机制拒绝机器人刷票。

##开发工具

IntelliJ IDEA  + Notepad++ + Git + Chrome

##压测工具

JMeter

##开发框架

前端：Bootstrap + jQuery + Thymeleaf

后端 ：SpringBoot + MyBatis + MySQL

中间件 : Druid + Redis + RabbitMQ 

##压测效果

本地电脑压测开启5000个线程，循环10次，327QPS（电脑性能不太好，压测效果不理想）

---

本项目来源于慕课网课程：[Java秒杀系统方案优化 高性能高并发实战](https://coding.imooc.com/class/168.html)

[代码参考](https://github.com/zaiyunduan123/springboot-seckill)
