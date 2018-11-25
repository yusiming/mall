package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台分类管理
 *
 * @Auther yusiming
 * @Date 2018/11/25 10:14
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 校验用户是否为管理员
     *
     * @param session session域
     * @return
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return user != null && user.getRole() != Const.Role.ROLE_CUSTOMER;
    }

    /**
     * 添加商品分类
     *
     * @param session      session域
     * @param categoryName 商品分类名称
     * @param parentId     父分类id
     * @return
     */
    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String categoryName,
                                              @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        if (isAdmin(session)) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
    }

    /**
     * 设置商品分类名称
     *
     * @param session      session域
     * @param categoryId   分类id
     * @param categoryName 分类名称
     * @return
     */
    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        if (isAdmin(session)) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return ServerResponse.createByErrorMessage("无权限，需要管理员权限");

    }

    /**
     * 查询某个商品分类下的一级分类，不递归查询
     *
     * @param session    session域
     * @param categoryId 商品分类id
     * @return
     */
    @RequestMapping(value = "get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",
            defaultValue = "0") Integer categoryId) {
        if (isAdmin(session)) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
    }

    /**
     * 递归查询指定分类及子分类的id
     *
     * @param session    session 域
     * @param categoryId 要递归查询的分类的id
     * @return
     */
    @RequestMapping(value = "get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId",
            defaultValue = "0") Integer categoryId) {
        if (isAdmin(session)) {
            // 查询
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
    }
}

