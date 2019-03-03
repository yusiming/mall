package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.vo.ProductDetailVo;

/**
 * @author yusiming
 * @date 2018/11/25 19:05
 */
public interface IProductService {
    /**
     * 保存或者更新商品信息
     * 如果Product存在id，则是更新商品信息，否则就是添加商品信息
     *
     * @param product 需要保存或者更新的商品信息
     * @return 如果保存或者更新商品信息成功，返回正确的响应，否则返回错误的响应
     */
    ServerResponse saveOrUpdateProduct(Product product);

    /**
     * 根据商品的id，设置商品的销售状态
     *
     * @param productId 商品id
     * @param status    商品状态
     * @return 如果设置商品状态成功，返回成功的响应，否则返回错误的响应
     */
    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    /**
     * 后台根据商品id查询商品详细信息
     *
     * @param productId 商品id
     * @return 如果查询成功，返回正确的响应，否则返回错误的响应
     */
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    /**
     * 后台查询所有商品，查询第pageNum页商品，每页pageSize个商品
     *
     * @param pageNum  第几页商品
     * @param pageSize 每页几条记录
     * @return 返回响应
     */
    ServerResponse getProductList(int pageNum, int pageSize);

    /**
     * 根据商品名称或者id查询商品信息
     * 如果是根据商品名称，可能需要分页
     *
     * @param productName 商品名称
     * @param productId   商品id
     * @param pageNum     第几页
     * @param pageSize    每页几条记录
     * @return 返回响应
     */
    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    /**
     * 根据商品id获取商品详细信息
     *
     * @param productId 商品id
     * @return 如果查询成功，返回正确的响应，否则返回错误的响应
     */
    ServerResponse getProductDetail(Integer productId);

    /**
     * 前台产品搜索以及动态排序
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
