package aop;

import java.lang.reflect.Proxy;

/**
 * Created by zhangxj on 2017/3/29.
 */
public class DynamicProxy {
    public static <T> T getInstance(Handler handler) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] intfs = handler.getTargetObject().getClass().getInterfaces();
        Object object = Proxy.newProxyInstance(classLoader, intfs, handler);
        return (T) object;
    }
}
