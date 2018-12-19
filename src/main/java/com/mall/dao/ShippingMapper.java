package com.mall.dao;

import com.mall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByShippingIdAndUserId(@Param("shippingId") Integer shippingId, @Param("userId") Integer userId);

    int updateByShippingIdAndUserId(Shipping record);

    Shipping selectByShippingIdAndUserId(@Param("shippingId") Integer shippingId, @Param("userId") Integer userId);

    List<Shipping> selectAllByUserId(Integer userId);
}