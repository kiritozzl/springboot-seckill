package org.seckill.service;

import org.seckill.bean.SeckillGoods;
import org.seckill.mapper.GoodsMapper;
import org.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    //乐观锁冲突最大重试次数
    private static final int DEFAULT_MAX_RETRIES = 5;

    @Autowired
    GoodsMapper goodsMapper;

    /**
     * 查询商品列表
     * @return
     */
    public List<GoodsVo> listGoodsVo(){
        return goodsMapper.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long id){
        return goodsMapper.getGoodsVoById(id);
    }

    /**
     * 减少库存，每次减一
     * @param goods
     * @return
     */
    public boolean reduceStock(GoodsVo goods) {
        int numAttempt = 0;
        int res = 0;
        SeckillGoods seckillGoods = new SeckillGoods(goods.getId(),goods.getId(),goods.getVersion());
        while (numAttempt < DEFAULT_MAX_RETRIES){
            numAttempt++;
            try{
                seckillGoods.setVersion(goodsMapper.getGoodsVersionById(goods.getId()));
                res = goodsMapper.reduceStockByVersion(seckillGoods);
//                res = goodsMapper.reduceStockByVersion(seckillGoods.getGoodsId(),seckillGoods.getVersion());
            } catch (Exception e){
                e.printStackTrace();
            }
            if (res != 0){
                break;
            }
        }
        return res > 0;
    }
}
