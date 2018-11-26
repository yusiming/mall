package com.mall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * FTP客户端工具
 *
 * @Auther yusiming
 * @Date 2018/11/25 21:46
 */
public class FTPUtil {
    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpPort = PropertiesUtil.getProperty("ftp.server.port");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPassword = PropertiesUtil.getProperty("ftp.password");
    private static FTPClient ftpClient;

    /**
     * 该方法暴露给外部使用
     *
     * @param fileList 要上传的文件列表
     * @return 如果上传成功返回true，否则返回false
     * @throws IOException 如果上传发生错误，抛出此异常
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        logger.info("开始连接ftp服务器");
        FTPUtil ftpUtil = new FTPUtil();
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("上传结束，上传结果:{}", result);
        return result;
    }

    /**
     * 上传文件
     *
     * @param remotePath ftp服务器目录
     * @param fileList   文件列表
     * @return 如果上传成功返回true，否则返回false
     * @throws IOException 如果上传发生错误，抛出异常异常
     */
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream inputStream = null;
        if (connectServer(ftpIp, Integer.parseInt(ftpPort), ftpUser, ftpPassword)) {
            try {
                // 切换目录，注意这里有一个坑，如果ftpuser对该目录没有w权限是不能将文件上传到该目录下的
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区大小
                ftpClient.setBufferSize(1024);
                // 设置编码
                ftpClient.setControlEncoding("UFT-8");
                // 设置文件类型，必须设置为已二进制文件来上传，否则图片会失真
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 设置本地的被动模式
                ftpClient.enterLocalPassiveMode();
                for (File file : fileList) {
                    inputStream = new FileInputStream(file);
                    // 存储文件
                    ftpClient.storeFile(file.getName(), inputStream);
                }
            } catch (IOException e) {
                logger.error("文件上传异常");
                e.printStackTrace();
                uploaded = false;
            } finally {
                // 关闭流
                if (inputStream != null) {
                    inputStream.close();
                }
                // 释放连接
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    /**
     * 连接ftp服务器
     *
     * @param ip       tp服务器ip
     * @param port     tp服务器使用的端口
     * @param user     用户
     * @param password 密码
     * @return 如果连接成功，返回true，否则返回false
     */
    private boolean connectServer(String ip, int port, String user, String password) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            // 连接服务器
            ftpClient.connect(ip);
            // 使用用户名称、密码登陆
            isSuccess = ftpClient.login(user, password);
        } catch (IOException e) {
            logger.error("连接ftp服务器异常", e);
        }
        return isSuccess;
    }
}
