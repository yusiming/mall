package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Category;

import java.util.List;

/**
 * @Auther yusiming
 * @Date 2018/11/25 10:34
 */
public interface ICategoryService {
    /**
     * 添加商品分类
     *
     * @param categoryName 商品分类名称
     * @param parentId     父id号
     * @return 若添加成功，返回成功的响应，否则返回错误的响应
     */
    ServerResponse addCategory(String categoryName, Integer parentId);

    /**
     * 根据商品分类的id，更新商品分类的名称
     *
     * @param categoryId   要更新的分类的id
     * @param categoryName 要更新的分类的名称
     * @return 如果更新成功，返回成功的响应，否则返回错误的响应
     */
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    /**
     * 根据商品id查询该分类下的子分类
     * 注意：不进行递归查询，只查询一层子分类，子分类的子分类不会被查询出来
     *
     * @param categoryId 分类的id
     * @return 包含一个集合的响应对象
     */
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
