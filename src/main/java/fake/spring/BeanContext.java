package fake.spring;

import fake.spring.anotation.*;
import fake.spring.aop.DynamicProxy;
import fake.spring.utils.AOPUtil;
import fake.spring.utils.ClassUtil;
import fake.spring.utils.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fake.spring.aop.Handler;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by zhangxj on 2017/3/27.
 */
public class BeanContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanContext.class);
    private static final String CLASS_PATH = BeanContext.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    private static final Map<Class, Object> clsToObject = new HashMap<>();
    private static final Map<String, Object> nameToObject = new HashMap<>();
    private static final Set<Class> aspects = new HashSet<>();
    private static final Set<Class> instanceClasses = new HashSet<>();//实例类（非aspect注解类）

    static {
        //递归的将类加载进JVM
        File file = new File(CLASS_PATH);
        if (file.isDirectory()) {
            String[] subDirs = Stream.of(file.listFiles((dir) -> {
                return dir.isDirectory();
            })).map(File::getName).toArray(String[]::new);
            Set<Class> clsSet = ClassUtil.load(subDirs);
            for (Class cls : clsSet) {

                //给每个Component注解修饰的类生成一个实例
                if (cls.isAnnotationPresent(Component.class) || cls.isAnnotationPresent(Aspect.class)) {
                    if (cls.isAnnotationPresent(Aspect.class)) {
                        aspects.add(cls);
                    } else if (!cls.isInterface()) {
                        instanceClasses.add(cls);
                    }
                    Object instanceObject = ReflectionUtil.instanceObject(cls);
                    clsToObject.put(cls, instanceObject);
                    //将该类的接口与该类的实例之间建立关系
                    Class[] interfaces = cls.getInterfaces();
                    if (interfaces != null) {
                        for (Class itf : interfaces) {
                            clsToObject.put(itf, instanceObject);
                        }
                    }
                    //建立类名到实例之间的关系
                    String beanName = null;
                    if (cls.getAnnotation(Component.class) != null) {
                        beanName = ((Component) cls.getAnnotation(Component.class)).name().trim();
                    }
                    beanName = beanName == null || beanName.equals("") ? cls.getName() : beanName;
                    nameToObject.put(beanName, instanceObject);
                }
            }
        }
        //给每个实例中被Autowired修饰的字段设置注入值
        for (Map.Entry<Class, Object> entry : clsToObject.entrySet()) {
            Class beanCls = entry.getKey();
            Object bean = entry.getValue();
            Field[] fields = beanCls.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    String autowiredBeanName = ((Autowired) field.getAnnotation(Autowired.class)).name().trim();
                    autowiredBeanName = autowiredBeanName == null || autowiredBeanName.equals("") ? field.getType().getName() : autowiredBeanName;
                    Object fieldValue = nameToObject.get(autowiredBeanName);
                    if (fieldValue == null) {
                        LOGGER.warn("cannot get bean named as '" + autowiredBeanName + "'");
                    }
                    ReflectionUtil.setField(bean, field, fieldValue);
                }
            }
        }
        //处理AOP，生成动态代理
        for (Class cls : instanceClasses) {
            Object instance = clsToObject.get(cls);
            //对每一个类都生成一个InvocationHandler实例
            Handler handler = new Handler(instance);
            for (Class aspect : aspects) {
                Method[] advices = aspect.getMethods();
                for (Method advice : advices) {
                    //如果该切面和通知能匹配到这个类中的任何一个方法，
                    // 就将该切面和通知存放入InvocationHandler中
                    if (AOPUtil.isAdvice(cls, advice)) {
                        handler.addAdvice(clsToObject.get(aspect), advice);
                    }
                }
            }
            //如果该InvocationHandler实例中确实有切面和通知，
            // 就生成其对应的动态代理对象，并用该动态代理对象去掉原先的实例
            if (handler.hasAdvice()){
                Object newInstance = DynamicProxy.getInstance(handler);
                for (Map.Entry<Class, Object> entry : clsToObject.entrySet()) {
                    if (entry.getValue()==instance){
                        entry.setValue(newInstance);
                    }
                }
                for (Map.Entry<String,Object> entry:nameToObject.entrySet()){
                    if (entry.getValue()==instance){
                        entry.setValue(newInstance);
                    }
                }
            }
        }
        LOGGER.info("Bean Context is already to be used!!!");
    }

    public static <T> T getBean(String beanName, Class<T> cls) {
        return (T) nameToObject.get(beanName);
    }

    public static <T> T getBean(Class<T> cls) {
        return (T) clsToObject.get(cls);
    }
}
