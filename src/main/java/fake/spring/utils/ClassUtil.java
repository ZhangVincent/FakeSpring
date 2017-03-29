package fake.spring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by vincent on 20/03/2017.
 */
public class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);
    private static final String CLASS_PATH = ClassUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    //递归的加载packageName下面的所有class文件  packageName的形式为xx.xx.xx.xx
    public static Set<Class> load(String[] packageNames) {
        HashSet<Class> set = new HashSet<Class>();
        for (String packageName : packageNames) {
            Set<Class> tmpSet = null;
            try {
                tmpSet = load(packageName, Thread.currentThread().getContextClassLoader());
            } catch (Exception e) {
                LOGGER.error("load class error:\n", e.getMessage());
            }
            if (tmpSet != null) {
                set.addAll(tmpSet);
            }
        }
        return set;
    }

    //packageName的形式为xx.xx.xx.xx
    public static Set<Class> load(final String packageName, ClassLoader classLoader) throws Exception {
        Set<Class> set = new HashSet<Class>();
        String packagePath = CLASS_PATH + packageName.replaceAll("\\.", "/");
        File file = new File(packagePath);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles(
                    (subFile) -> {
                        if (subFile.isDirectory() || subFile.getName().endsWith(".class")) {
                            return true;
                        } else {
                            return false;
                        }
                    });
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    set.addAll(load(packageName + "." + subFile.getName(), classLoader));
                } else {
                    String clsName = subFile.getName();
                    clsName = clsName.substring(0, clsName.lastIndexOf(".class"));
                    String subClsPath = packageName + "." + clsName;
                    set.add(doLoadClass(subClsPath, classLoader));
                }
            }
        }
        return set;
    }


    private static Class doLoadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }


}
