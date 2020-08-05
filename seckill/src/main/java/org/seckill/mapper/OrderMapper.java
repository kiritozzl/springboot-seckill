package org.seckill.mapper;

import org.apache.ibatis.annotations.*;
import org.seckill.bean.OrderInfo;
import org.seckill.bean.SeckillOrder;

@Mapper
public interface OrderMapper {

    @Select("select * from sk_order where user_id = #{userId} and goods_id = #{goodsId}")
    public SeckillOrder getOrderByUserIdGoodsId(@Param("userId")long userId, @Param("goodsId")long goodsId);

    @Insert("insert into sk_order_info (user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date) values" +
            "(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,
    statement = "select last_insert_id()")
    public long insert(OrderInfo orderInfo);

    @Insert("insert into sk_order (user_id, goods_id, order_id) values" +
            "(#{userId},#{goodsId},#{orderId})")
    public int insertSeckillOrder(SeckillOrder order);

    @Select("select * from sk_order_info where id = #{orderId}")
    public OrderInfo getOrderByGoodsId(@Param("orderId") long orderId);

    @Delete("delete soi, so from sk_order_info soi inner join sk_order so on soi.id = so.id")
    public  int clearOrderData();
}
