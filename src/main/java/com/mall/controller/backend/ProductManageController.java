package com.mall.controller.backend;

import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IFileService;
import com.mall.service.IProductService;
import com.mall.util.CookieUtil;
import com.mall.util.JsonUtil;
import com.mall.util.PropertiesUtil;
import com.mall.util.ShardedRedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 后台商品管理
 *
 * @author yusiming
 * @date 2018/11/25 18:54
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 校验用户是否为管理员
     *
     * @param request request
     * @return 如果用户未登陆或者不是管理员，返回错误的响应，否则返回成功的响应
     */
    private ServerResponse checkAdmin(HttpServletRequest request) {
        String token = CookieUtil.getLoginCookie(request);
        if (token != null) {
            User user = JsonUtil.stringToObj(ShardedRedisPoolUtil.get(token), User.class);
            if (user != null && user.getRole().equals(Const.Role.ROLE_ADMIN)) {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByErrorMessage("您不是管理员，请勿随意登陆!");
        }
        return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 保存商品信息
     *
     * @param request request
     * @param product 通过springMVC参数绑定得到的商品对象
     * @return 给前台的响应
     */
    @RequestMapping("save_product.do")
    @ResponseBody
    public ServerResponse productSave(HttpServletRequest request, Product product) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iProductService.saveOrUpdateProduct(product);
        }
        return response;
    }

    /**
     * 设置商品的销售状态（产品上下架）
     *
     * @param request   request
     * @param productId 商品的id
     * @param status    商品新的的状态
     * @return 响应
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpServletRequest request, Integer productId, Integer status) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iProductService.setSaleStatus(productId, status);
        }
        return response;
    }

    /**
     * 获取商品详细信息
     *
     * @param request   request
     * @param productId 商品id
     * @return 响应
     */
    @RequestMapping("get_product_detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpServletRequest request, Integer productId) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iProductService.manageProductDetail(productId);
        }
        return response;
    }

    /**
     * 查询所有商品
     * 需要分页，默认查询第一页，每页10条记录
     *
     * @param request  request
     * @param pageNum  第几页
     * @param pageSize 每页的记录数
     * @return 响应
     */
    @RequestMapping("get_product_list.do")
    @ResponseBody
    public ServerResponse getProductList(HttpServletRequest request,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        }
        return response;
    }

    /**
     * @param request     request
     * @param productName 需要查询的商品名称
     * @param productId   需要查询的商品id
     * @param pageNum     第几页
     * @param pageSize    每页几条数据
     * @return 响应
     */
    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse productSearch(HttpServletRequest request, String productName, Integer productId,
                                        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }
        return response;
    }

    /**
     * 图片上传
     *
     * @param file    MultipartFile
     * @param request request
     * @return 响应，包含uri和url
     */
    @RequestMapping(value = "upload_image.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "uploadFile") MultipartFile file,
                                 HttpServletRequest request) {
        ServerResponse response = checkAdmin(request);
        if (response.isSuccess()) {
            String upload = request.getServletContext().getRealPath("upload");
            // 将文件上传到ftp服务中，返回targetFileName，上传文件的名称
            String targetFileName = iFileService.upload(file, upload);
            // 文件的完整地址，可以访问的地址
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map<String, String> map = Maps.newHashMap();
            map.put("uri", targetFileName);
            map.put("url", url);
            return ServerResponse.createBySuccess(map);
        }
        return response;
    }

    /**
     * 富文本图片上传
     *
     * @param file            MultipartFile
     * @param request         request
     * @param servletResponse HttpServletResponse
     * @return 返回simditor要求的响应
     */
    @RequestMapping(value = "richText_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(@RequestParam(value = "uploadFile", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse servletResponse) {
        // 富文本中对于返回值有特定的要求，我们使用的simditor，所以需要按照它的要求进行返回，simditor接收到返回值会进行判断上传图片是否成功之类的
        // JSON response after uploading complete:
        // {
        //   "success": true/false,
        //   "msg": "error message", # optional
        //   "file_path": "[real file path]"
        // }
        Map<String, Object> map = Maps.newHashMap();
        ServerResponse serverResponse = checkAdmin(request);
        User user = (User) serverResponse.getData();
        if (user == null) {
            map.put("success", false);
            map.put("msg", "未登陆");
            return map;
        }
        if (user.getRole() == Const.Role.ROLE_ADMIN) {
            String upload = request.getServletContext().getRealPath("upload");
            // 将文件上传到ftp服务中，返回targetFileName，上传文件的名称
            String targetFileName = iFileService.upload(file, upload);
            // 文件的完整地址，可以访问的地址
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            map.put("success", true);
            map.put("msg", "上传成功");
            map.put("file_path", url);
            // 必须要添加一个响应头
            servletResponse.addHeader("Access-Controller-Allow-Headers", "X-File-Name");
            return map;
        } else {
            map.put("success", false);
            map.put("msg", "无权限操作");
            return map;
        }
    }

}
