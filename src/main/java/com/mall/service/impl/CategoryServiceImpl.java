package com.mall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.pojo.Category;
import com.mall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 商品分类服务
 *
 * @author yusiming
 * @date 2018/11/25 10:35
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    /**
     * @param categoryName 商品分类名称
     * @param parentId     父id号
     * @return 响应
     */
    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        // 这里不能使用insertSelective方法，因为我们要让数据库生成create_time和update_time字段
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("添加分类成功");
        }
        return ServerResponse.createByErrorMessage("添加分类失败");
    }

    /**
     * 根据商品分类id，更新分类名称
     *
     * @param categoryId   要更新的分类的id
     * @param categoryName 要更新的分类的名称
     * @return 响应
     */
    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        category.setUpdateTime(new Date());
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("更新分类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");
    }

    /**
     * 根据商品id查询该分类下的子分类
     *
     * @param categoryId 分类的id
     * @return 响应
     */
    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        if (categoryId == null) {
            return ServerResponse.createByErrorMessage("参数错误，分类id不能为空");
        }
        // 注意：这里即使没有查询到任何子分类，也不认为是逻辑错误，直接返回一个空的集合即可
        List<Category> list = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        if (CollectionUtils.isEmpty(list)) {
            logger.info("未找到当前分类的子分类:" + categoryId);
        }
        return ServerResponse.createBySuccess(list);
    }

    /**
     * 递归查询指定分类的所有子分类，包括子孙分类
     *
     * @param categoryId 要查询的分类的id
     * @return 响应
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        if (categoryId == null) {
            return ServerResponse.createByErrorMessage("参数错误，分类id不能为空");
        }
        // 这里使用一个set来存储所有的分类，确保不会出现相同的分类
        Set<Category> categorySet = Sets.newHashSet();
        this.findChildCategory(categorySet, categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        for (Category category : categorySet) {
            categoryIdList.add(category.getId());
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 递归算法，查询分类以及子分类
     *
     * @param categorySet set集合用来保存查询出的分类
     * @param categoryId  分类id
     */
    private void findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
    }

}
