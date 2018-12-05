package com.mall.dao;

import com.mall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectAllByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") long orderNo);

    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    void updateProductStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    List<OrderItem> selectAllByOrderNo(Long orderNo);
}