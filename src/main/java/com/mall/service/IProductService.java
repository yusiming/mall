package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.vo.ProductDetailVo;

/**
 * @Auther yusiming
 * @Date 2018/11/25 19:05
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse getProductList(int pageNum, int pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    /**
     * 根据商品id获取商品详细信息
     *
     * @param productId 商品id
     * @return 如果查询成功，返回正确的响应，否则返回错误的响应
     */
    ServerResponse getProductDetail(Integer productId);

    /**
     * 产品搜索以及动态排序
     *
     * @param keyword    关键字
     * @param categoryId 分类id
     * @param pageNum    页号
     * @param pageSize   每页几条数据
     * @param orderBy    排序方式
     * @return 响应
     */
    ServerResponse<PageInfo> getProductsByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,
                                                          String orderBy);
}
