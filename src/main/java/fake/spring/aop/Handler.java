package fake.spring.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fake.spring.aop.bean.AspectAndAdvice;
import fake.spring.utils.AOPUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangxj on 2017/3/29.
 */
public class Handler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    //带动态代理的对象
    private Object targetObject;
    //targetObject的所有切面和通知
    private Set<AspectAndAdvice> advices = new HashSet<>();


    public Handler(Object target) {
        this.targetObject = target;
    }

    public void addAdvice(Object aspectObject, Method advice) {
        this.advices.add(new AspectAndAdvice(aspectObject, advice));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method joinPoint = targetObject.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());

        //调用对应的前置通知
        for (AspectAndAdvice aspectAndAdvice : advices) {
            Object aspect = aspectAndAdvice.getAspectObject();
            Method advice = aspectAndAdvice.getAdvice();
            if (AOPUtil.isBeforeAdvice(joinPoint, advice)) {
                //是method的前置通知
                if (advice.getParameterTypes().length > 0) {
                    advice.invoke(aspect, args);
                } else {
                    advice.invoke(aspect);
                }
            }
        }
        //连接点被调用
        Object result = method.invoke(targetObject, args);
        //调用对应的后置通知
        for (AspectAndAdvice aspectAndAdvice : advices) {
            Object aspect = aspectAndAdvice.getAspectObject();
            Method advice = aspectAndAdvice.getAdvice();
            if (AOPUtil.isAfterAdvice(joinPoint, advice)) {
                //是method的后置通知
                if (advice.getParameterTypes().length > 0) {
                    advice.invoke(aspect, args);
                } else {
                    advice.invoke(aspect);
                }
            }
        }
        return result;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public boolean hasAdvice() {
        return this.advices.size() > 0 ? true : false;
    }
}
