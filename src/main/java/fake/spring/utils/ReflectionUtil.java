package fake.spring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by zhangxj on 2017/3/27.
 */
public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);


    public static Object instanceObject(Class cls) {
        Object object = null;
        try {
            object = cls.newInstance();
        } catch (Exception e) {
            LOGGER.error("class instance failed:\n" + e.getMessage());
        }
        return object;
    }

    public static void setField(Object targetObject, Field field, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(targetObject, fieldValue);
        } catch (IllegalAccessException e) {
            LOGGER.error("set field failed:\n" + e.getMessage());
        }
    }

    public static Object invokeMethod(Object targetObject, Method method, Object... args) {
        Object result = null;
        try {
            method.setAccessible(true);
            result = method.invoke(targetObject, args);
        } catch (Exception e) {
            LOGGER.error("invoke method failed:\n" + e.getMessage());
        }
        return result;
    }

}
