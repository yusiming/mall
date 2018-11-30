package com.mall.controller.backend;

import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IFileService;
import com.mall.service.IProductService;
import com.mall.service.IUserService;
import com.mall.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 后台商品管理
 *
 * @Auther yusiming
 * @Date 2018/11/25 18:54
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 保存商品信息
     *
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save_product.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session,
                                      @RequestParam(value = "product") Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 设置商品的销售状态
     *
     * @param session   session域对象
     * @param productId 商品的id
     * @param status    商品新的的状态
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取商品详细信息
     *
     * @param session   session
     * @param productId 商品id
     * @return
     */
    @RequestMapping("get_product_detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("get_product_list.do")
    @ResponseBody
    public ServerResponse getProductList(HttpSession session,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 根据商品名称、商品id搜索商品
     *
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId,
                                        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 图片上传
     *
     * @param file
     * @param session
     * @return
     */
    @RequestMapping(value = "upload_image.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "uploadFile") MultipartFile file,
                                 HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String upload = session.getServletContext().getRealPath("upload");
            // 将文件上传到ftp服务中，返回targetFileName，上传文件的名称
            String targetFileName = iFileService.upload(file, upload);
            // 文件的完整地址，可以访问的地址
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map<String, String> map = Maps.newHashMap();
            map.put("uri", targetFileName);
            map.put("url", url);
            return ServerResponse.createBySuccess(map);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping(value = "richText_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(@RequestParam(value = "uploadFile", required = false) MultipartFile file,
                                 HttpSession session, HttpServletResponse response) {
        // 富文本中对于返回值有特定的要求，我们使用的simditor，所以需要按照它的要求进行返回，simditor接收到返回值会进行判断上传图片是否成功之类的
        // JSON response after uploading complete:
        // {
        //   "success": true/false,
        //   "msg": "error message", # optional
        //   "file_path": "[real file path]"
        // }
        Map<String, Object> map = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            map.put("success", false);
            map.put("msg", "未登陆");
            return map;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String upload = session.getServletContext().getRealPath("upload");
            // 将文件上传到ftp服务中，返回targetFileName，上传文件的名称
            String targetFileName = iFileService.upload(file, upload);
            // 文件的完整地址，可以访问的地址
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            map.put("success", true);
            map.put("msg", "上传成功");
            map.put("file_path", url);
            response.addHeader("Access-Controller-Allow-Headers", "X-File-Name");
            return map;
        } else {
            map.put("success", false);
            map.put("msg", "无权限操作");
            return map;
        }
    }

}