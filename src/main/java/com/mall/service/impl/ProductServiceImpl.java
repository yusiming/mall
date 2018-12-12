package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Category;
import com.mall.pojo.Product;
import com.mall.service.ICategoryService;
import com.mall.service.IProductService;
import com.mall.util.DateTimeUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.ProductDetailVo;
import com.mall.vo.ProductListVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther yusiming
 * @Date 2018/11/25 19:06
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 新增或者更新商品，如果product有id，则更新商品信息，否则新增商品
     *
     * @param product 商品对象
     * @return 响应
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 设置商品主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImagesArray = product.getSubImages().split(",");
                product.setMainImage(subImagesArray[0]);
            }
            if (product.getId() != null) {
                if (productMapper.updateByPrimaryKey(product) > 0) {
                    return ServerResponse.createBySuccessMsg("更新商品信息成功");
                } else {
                    return ServerResponse.createBySuccessMsg("更新商品信息失败");
                }
            } else {
                if (productMapper.insert(product) > 0) {
                    return ServerResponse.createBySuccessMsg("新增商品成功");
                } else {
                    return ServerResponse.createBySuccessMsg("新增商品失败");
                }
            }
        }
        return ServerResponse.createByErrorMessage("新增或者更新产品参数错误");
    }

    /**
     * 修改商品状态
     *
     * @param productId 商品id
     * @param status    商品新的状态
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        if (productMapper.updateByPrimaryKeySelective(product) > 0) {
            return ServerResponse.createBySuccessMsg("修改商品状态成功");
        }
        return ServerResponse.createByErrorMessage("修改商品状态失败");
    }

    /**
     * 查询商品详情
     *
     * @param productId 商品id
     * @return 方法将返回包装了的vo的响应
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已下架或者被删除了");
        }
        return ServerResponse.createBySuccess(assembleProductDetailVo(product));
    }

    /**
     * 通过Product对象包装ProductDetailVo
     *
     * @param product 商品对象
     * @return ProductDetailVo 商品vo
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        // imageHost，如果没有读取到值，则使用http://image.mall.com
        // TODO: 2018/12/12 linux本地host需要设置 http://image.mall.com 对应 127.0.0.1
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.mall.com/"));
        // parentCategoryId
        Integer parentCategoryId = categoryMapper.getParentCategoryId(product.getCategoryId());
        if (parentCategoryId == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(parentCategoryId);
        }
        // createTime
        productDetailVo.setCreateTime(DateTimeUtil.DateToStr(product.getCreateTime()));
        // updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.DateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        // startPage
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            productListVoList.add(this.assembleProductListVo(product));
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setStatus(product.getStatus());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.mall.com/"));
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.offsetPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            productListVoList.add(this.assembleProductListVo(product));
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 根据商品id查询商品信息
     *
     * @param productId 商品id
     * @return 响应
     */
    public ServerResponse getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品不存在");
        }
        if (product.getStatus() != Const.ProductStatus.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("商品已下架或者被删除了");
        }
        return ServerResponse.createBySuccess(assembleProductDetailVo(product));
    }

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
    public ServerResponse<PageInfo> getProductsByKeywordCategory(String keyword, Integer categoryId, int pageNum,
                                                                 int pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            // 如果分类不存在，并且关键字也为空，这里不会返回错误提示信息，返回一个空的PageInfo
            if (category == null && StringUtils.isEmpty(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo<ProductListVo> pageInfo = new PageInfo<>(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            // 如果分类存在，获取分类下的所有子分类
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        // 排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArr = orderBy.split("_");
                // PageHelper的orderBy 方法的参数格式 "price desc"
                PageHelper.orderBy(orderByArr[0] + " " + orderByArr[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isNotBlank(keyword) ? keyword : null,
                categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            productListVoList.add(assembleProductListVo(product));
        }
        // System.out.println(productList.getClass().getName());
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}

