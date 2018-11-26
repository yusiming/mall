package com.mall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Auther yusiming
 * @Date 2018/11/25 21:29
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
