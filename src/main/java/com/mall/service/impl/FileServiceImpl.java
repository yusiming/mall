package com.mall.service.impl;

import com.google.common.collect.Lists;
import com.mall.service.IFileService;
import com.mall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Auther yusiming
 * @Date 2018/11/25 21:30
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        // 获取原始文件名
        String fileName = file.getOriginalFilename();
        // 获取文件拓展名称
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 为了防止上传文件名称冲突使用UUID+拓展名，生成上传文件名称
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传的文件名:{},上传的路径:{}，新文件名:{}", fileName, path, uploadFileName);
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            // 如果该目录不存在创建之
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);
        try {
            file.transferTo(targetFile);
            // 将文件上传到ftp服务器中
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 删除临时文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常", e);
        }
        // 返回上传文件名称
        return targetFile.getName();
    }
}
