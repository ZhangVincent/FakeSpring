package utils;

import anotation.Autowired;
import anotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
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

    static {
        //给每个Component注解修饰的类生成一个实例
        File file = new File(CLASS_PATH);
        if (file.isDirectory()) {
            String[] subDirs = Stream.of(file.listFiles((dir) -> {
                return dir.isDirectory();
            })).map(File::getName).toArray(String[]::new);
            Set<Class> clsSet = ClassUtil.load(subDirs);
            for (Class cls : clsSet) {
                if (cls.isAnnotationPresent(Component.class)) {
                    Object instanceObject = ReflectionUtil.instanceObject(cls);
                    clsToObject.put(cls, instanceObject);
                    String beanName = ((Component) cls.getAnnotation(Component.class)).name().trim();
                    beanName = beanName == null || beanName.equals("") ? cls.getName() : beanName;
                    nameToObject.put(beanName,instanceObject);
                }
            }
        }
        //给每个实例中被Autowired修饰的字段设置注入值
        for (Map.Entry<Class,Object> entry:clsToObject.entrySet()){
            Class beanCls = entry.getKey();
            Object bean = entry.getValue();
            Field[] fields = beanCls.getDeclaredFields();
            for (Field field:fields){
                if (field.isAnnotationPresent(Autowired.class)){
                    String autowiredBeanName = ((Autowired)field.getAnnotation(Autowired.class)).name().trim();
                    autowiredBeanName = autowiredBeanName==null||autowiredBeanName.equals("")?field.getType().getName():autowiredBeanName;
                    Object fieldValue = nameToObject.get(autowiredBeanName);
                    if (fieldValue==null){
                        LOGGER.warn("cannot get bean named as '"+autowiredBeanName+"'");
                    }
                    ReflectionUtil.setField(bean,field,fieldValue);
                }
            }
        }
    }

    public static <T> T getBean(String beanName,Class<T> cls){
        return (T)nameToObject.get(beanName);
    }

    public static <T> T getBean(Class<T> cls){
        return (T)clsToObject.get(cls);
    }


}
