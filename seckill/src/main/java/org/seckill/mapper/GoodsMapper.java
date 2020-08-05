package org.seckill.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.seckill.bean.SeckillGoods;
import org.seckill.vo.GoodsVo;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Mapper
public interface GoodsMapper {

    @Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price, sg.version " +
            "from sk_goods_seckill sg left join sk_goods g on sg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price, sg.version " +
            "from sk_goods_seckill sg left join sk_goods g on sg.goods_id = g.id where g.id = #{id}")
    public GoodsVo getGoodsVoById(@Param("id") long id);

    @Select("select version from sk_goods_seckill where goods_id = #{id}")
    public int getGoodsVersionById(@Param("id") long id);

    //stock_count > 0 和 版本号实现乐观锁 防止超卖
    @Update("update sk_goods_seckill set stock_count = stock_count - 1, version= version + 1 where goods_id = #{goodsId} and stock_count > 0 and version = #{version}")
//    public int reduceStockByVersion(@Param("goodsId")long goodsId, @Param("version")int version);
    public int reduceStockByVersion(SeckillGoods seckillGoods);

    @Update("update sk_goods_seckill set stock_count = 50 where goods_id = 1")
    public int updateStock();
}
