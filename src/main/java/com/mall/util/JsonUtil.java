package com.mall.util;

import com.google.common.collect.Lists;
import com.mall.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @Auther yusiming
 * @Date 2018/12/30 10:59
 */
public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    /**
     * 序列化和反序列化的关键对象
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        /*
         * ALWAYS：对象的所有属性都会被序列化，包括值为空的属性
         * NON_NULL：只有非空的属性才会被序列化
         * NON_DEFAULT：只有值不为默认值的时候才会被序列化
         * NON_EMPTY：所有empty的对象都不会被序列化，NON_EMPTY比NON_NULL更加严格
         */
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
        // 取消默认的时间戳格式，该属性默认为true
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 忽略序列化空bean（bean的任何属性都没有被赋值）时的错误，
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        // 统一日期格式“yyyy-MM-dd HH:mm:ss”
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
        // 忽略在反序列化时，找不到对应的属性时发生的错误，该属性默认值为true
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将对象序列化为json字符串
     *
     * @param obj 被序列化的对象
     * @param <T> 类型
     * @return 对象序列化之后的字符串
     */
    public static <T> String objToString(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("parse object to string error", e);
            return null;
        }
    }

    /**
     * 将对象序列化为已经格式化好的json字符串
     *
     * @param obj 被序列化的对象
     * @param <T> 类型
     * @return 对象序列化之后的字符串
     */
    public static <T> String objToStringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("parse object to string error", e);
            return null;
        }
    }

    /**
     * 将json字符串反序列化为指定的对象
     *
     * @param s     反序列化的字符串
     * @param clazz 对象的类型
     * @param <T>   类型变量
     * @return 被反序列化之后得到的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToObj(String s, Class<T> clazz) {
        if (StringUtils.isEmpty(s) || clazz == null) {
            return null;
        }
        try {
            return clazz == String.class ? (T) s : objectMapper.readValue(s, clazz);
        } catch (Exception e) {
            logger.warn("parse string to object error", e);
            return null;
        }
    }

    /**
     * 将json字符串类型反序列化为指定的任意类型的对象
     *
     * @param s             字符串
     * @param typeReference 对象的类型
     * @param <T>           类型变量
     * @return 反序列化之后得到的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToObj(String s, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(s) || typeReference == null) {
            return null;
        }
        try {
            return typeReference.getType() == String.class ? (T) s : objectMapper.readValue(s, typeReference);
        } catch (Exception e) {
            logger.warn("parse string to object error", e);
            return null;
        }
    }

    /**
     * 将字符串反序列化为集合类型的对象
     *
     * @param s               json字符串
     * @param collectionClass 集合的类型
     * @param elementClasses  集合中对象的类型
     * @param <T>             类型变量
     * @return 反序列化之后的得到的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToObj(String s, Class<?> collectionClass, Class<?>... elementClasses) {
        if (StringUtils.isEmpty(s) || collectionClass == null || elementClasses == null) {
            return null;
        }
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(s, javaType);
        } catch (Exception e) {
            logger.warn("parse string to object error", e);
            return null;
        }
    }

    public static void main(String[] args) {
        test2();
    }

    public static void test1() {
        User u1 = new User();
        u1.setId(1);
        u1.setUsername("yusiming");

        User u2 = new User();
        u2.setId(2);
        u2.setUsername("yuxiaoming");

        String u1Json = JsonUtil.objToString(u1);
        String u1JsonPretty = JsonUtil.objToStringPretty(u1);
        logger.info("u1Json: {}", u1Json);
        logger.info("u1JsonPretty: {}", u1JsonPretty);

        User user = JsonUtil.stringToObj(u1Json, User.class);

        List<User> userList = Lists.newArrayList();
        userList.add(u1);
        userList.add(u2);
        String userListJson = JsonUtil.objToStringPretty(userList);
        logger.info("====================");
        logger.info(userListJson);
        // 反序列化失败
        List list = JsonUtil.stringToObj(userListJson, List.class);
        // 反序列化成功
        list = JsonUtil.stringToObj(userListJson, new TypeReference<List<User>>() {
        });
        // 反序列化成功
        list = JsonUtil.stringToObj(userListJson, List.class, User.class);
        System.out.println("test end");
    }

    public static void test2() {

    }
}

