package com.mall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author yusiming
 * @date 2018/11/25 21:29
 */
public interface IFileService {
    /**
     * 上传文件到ftp服务器上
     *
     * @param file MultipartFile
     * @param path 上传文件的路径
     * @return 响应
     */
    String upload(MultipartFile file, String path);
}
