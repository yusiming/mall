package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.dao.ShippingMapper;
import com.mall.pojo.Shipping;
import com.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther yusiming
 * @Date 2018/11/30 19:18
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 根据用户id添加收货地址
     *
     * @param userId   用户id
     * @param shipping 简单对象
     * @return 响应
     */
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        // 使用mybatis获取自增主键，shipping中就有id了
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            Map<String, Integer> map = new HashMap<>();
            map.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccessMsg("新建地址成功", map);
        }
        return ServerResponse.createBySuccessMsg("新建地址失败");
    }

    /**
     * 删除用户收货地址
     *
     * @param userId     用户id
     * @param shippingId 说货地址id
     * @return 响应
     */
    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        // shippingMapper.deleteByPrimaryKey() 如果这样写，是有安全漏洞的，用户可横向越权，删除其他人的收货地址
        int resultCount = shippingMapper.deleteByShippingIdAndUserId(shippingId, userId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    /**
     * 更新用户地址信息
     *
     * @param userId   用户id
     * @param shipping 地址对象
     * @return 响应
     */
    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        // 注意，这里要将session中拿到的id，赋值给shipping对象，以为前端传过来的id是可以模拟的，有可能收到加的userId
        shipping.setUserId(userId);
        // 更新用户地址时，也存在用户横向越权的问题
        int resultCount = shippingMapper.updateByShippingIdAndUserId(shipping);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("更新地址信息成功");
        }
        return ServerResponse.createByErrorMessage("更新地址信息失败");
    }

    @Override
    public ServerResponse select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByShippingIdAndUserId(shippingId, userId);
        if (shipping != null) {
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByErrorMessage("查询不到该地址信息");
    }

    @Override
    public ServerResponse list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectAllByUserId(userId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
