package fake.spring.aop.bean;

import java.lang.reflect.Method;

/**
 * Created by zhangxj on 2017/3/29.
 */
public class AspectAndAdvice {
    Object aspectObject;
    Method advice;

    public AspectAndAdvice(Object aspectObject, Method advice) {
        this.aspectObject = aspectObject;
        this.advice = advice;
    }

    public Object getAspectObject() {
        return aspectObject;
    }

    public void setAspectObject(Object aspectObject) {
        this.aspectObject = aspectObject;
    }

    public Method getAdvice() {
        return advice;
    }

    public void setAdvice(Method advice) {
        this.advice = advice;
    }
}
