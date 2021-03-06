package com.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 前台商品接口
 *
 * @author yusiming
 * @date 2018/11/26 21:25
 */
@Controller()
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 根据商品id查询商品详细信息
     *
     * @param productId 商品id
     * @return 响应
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(Integer productId) {
        return iProductService.getProductDetail(productId);
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
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {
        return iProductService.getProductsByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
