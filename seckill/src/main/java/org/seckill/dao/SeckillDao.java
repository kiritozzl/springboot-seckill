package org.seckill.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillDao {

    @Select("select * from seckill where seckill_id = #{id}")
    public Seckill getById(@Param("id") long id);

    @Insert("insert into seckill(seckill_id, name, number, start_time, end_time) values " +
            "(#{seckillId},#{name},#{number},#{startTime},#{endTime})")
    public int insert(Seckill seckill);
}
