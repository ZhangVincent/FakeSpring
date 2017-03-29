package fake.spring.utils;

import fake.spring.anotation.After;
import fake.spring.anotation.Before;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangxj on 2017/3/29.
 */
public class AOPUtil {


    public static boolean isAdvice(Class targetClass, Method advice) {

        Method[] methods = targetClass.getMethods();
        for (Method method:methods){
            if (isBeforeAdvice(method,advice)||isAfterAdvice(method,advice)){
                return true;
            }
        }
        return false;
    }


    //判断是否是前置通知
    public static boolean isBeforeAdvice(Method joinPoint, Method advice) {
        if (advice.isAnnotationPresent(Before.class)) {
            String adviceJoinPointStr = advice.getAnnotation(Before.class).joinPoint();
            String joinPointStr = joinPoint.getDeclaringClass().getName() + "." + joinPoint.getName();
            Pattern p = Pattern.compile(adviceJoinPointStr);
            Matcher m = p.matcher(joinPointStr);
            return m.matches();
        }
        return false;
    }

    //判断是否是后置通知
    public static boolean isAfterAdvice(Method joinPoint, Method advice) {
        if (advice.isAnnotationPresent(After.class)) {
            String adviceJoinPointStr = advice.getAnnotation(After.class).joinPoint();
            String joinPointStr = joinPoint.getDeclaringClass().getName() + "." + joinPoint.getName();
            Pattern p = Pattern.compile(adviceJoinPointStr);
            Matcher m = p.matcher(joinPointStr);
            return m.matches();
        }
        return false;
    }


}
