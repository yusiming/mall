package com.mall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 读取配置文件信息
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(Properties.class);

    private static Properties props;

    // 加载配置文件
    static {
        String fileName = "mall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),
                    "UTF-8"));
        } catch (IOException e) {
            logger.error("读取配置信息文件异常");
        }
    }

    /**
     * 读取指定的配置项
     *
     * @param key key
     * @return key对的值
     */
    public static String getProperty(String key) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 读取指定的配置项，如果没有读取到，则使用默认的值defaultValue
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return 读取到的值或则传入的默认值
     */
    public static String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return value.trim();
    }
}
