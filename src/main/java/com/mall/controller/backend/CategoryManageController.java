package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台分类管理接口
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
     * @return 如果用户未登陆或者不是管理员，返回错误的响应，否则返回成功的响应
     */
    private ServerResponse checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        } else if (user.getRole() != Const.Role.ROLE_ADMIN) {
            return ServerResponse.createByErrorMessage("您不是管理员，请勿随意登陆，否则我们将封禁您的IP!");
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 添加商品分类
     * 注意：若没有传入parentId，则赋予一个默认值0，表示这是一个一级分类
     *
     * @param session      session域
     * @param categoryName 商品分类名称
     * @param parentId     父分类id
     * @return 响应
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        ServerResponse response = checkAdmin(session);
        if (response.isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return response;
    }

    /**
     * 设置商品分类名称
     *
     * @param session      session域
     * @param categoryId   分类id
     * @param categoryName 分类名称
     * @return 响应
     */
    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        ServerResponse response = checkAdmin(session);
        if (response.isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return response;

    }

    /**
     * 查询某个商品分类下的一层子分类，不递归查询
     *
     * @param session    session域
     * @param categoryId 商品分类id
     * @return 响应
     */
    @RequestMapping(value = "get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId",
            defaultValue = "0") Integer categoryId) {
        ServerResponse response = checkAdmin(session);
        if (response.isSuccess()) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return response;
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
        ServerResponse response = checkAdmin(session);
        if (response.isSuccess()) {
            // 查询
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return response;
    }
}

