package com.mall.dao;

import com.mall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectOrderByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") long orderNo);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectAllOrder();

    int selectOrderByOrderNoAndTotalAmount(@Param("orderNo") String orderNo, @Param("payment") String payment);
}