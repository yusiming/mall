package com.mall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CartMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Cart;
import com.mall.pojo.Product;
import com.mall.service.ICartService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.CartProductVo;
import com.mall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Auther yusiming
 * @Date 2018/11/29 19:22
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 将商品添加到购物车中
     *
     * @param userId    用户id
     * @param productId 商品id
     * @param count     商品数量
     * @return
     */
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        // 如果cart为null，证明这个商品不该用户的购物车中，需要新增一个这个商品的记录，并插入到Cart表中
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(count);
            cart.setChecked(Const.Cart.CHECKED);
            // 持久化到数据库中
            cartMapper.insert(cart);
        } else {
            // 如果cart不为null，证明这个用户的购物车中已经存在该商品了，将数据量相加即可
            cart.setQuantity(cart.getQuantity() + count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    public ServerResponse update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    @Override
    public ServerResponse delete(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdAndProducts(userId, productIdList);
        return this.list(userId);
    }

    public ServerResponse list(Integer userId) {
        return ServerResponse.createBySuccess(this.getCartVoLimit(userId));
    }

    @Override
    public ServerResponse selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUnCheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    public ServerResponse getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));

    }

    public CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cart : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                // 添加有关购物车的信息
                cartProductVo.setId(cart.getId());
                cartProductVo.setUserId(cart.getUserId());
                cartProductVo.setProductId(cart.getProductId());

                // 添加有关商品的信息
                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductPrice(product.getPrice());

                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());

                    // 判断库存数量
                    int buyLimitCount = 0;
                    if (product.getStock() >= cart.getQuantity()) {
                        buyLimitCount = cart.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        // 更新购物车中的数量为product的stock
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cart.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    // 计算单个商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),
                            buyLimitCount));
                    // 设置勾选状态
                    cartProductVo.setProductChecked(cart.getChecked());
                }
                // 如果商品是勾选状态，增加购物车的总价中
                if (cart.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),
                            cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
