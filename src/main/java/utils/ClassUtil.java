package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vincent on 20/03/2017.
 */
public class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);


    //递归的加载packageName下面的所有class文件  packageName的形式为xx.xx.xx.xx
    public static HashSet<Class> load(String[] packageNames, ClassLoader classLoader) {
        HashSet<Class> set = new HashSet<Class>();
        for (String packageName : packageNames) {
            HashSet<Class> tmpSet = null;
            try {
                tmpSet = load(packageName, classLoader);
            } catch (Exception e) {
                LOGGER.error("load class error", e.getMessage());
                tmpSet = new HashSet<Class>();
            }
            set.addAll(tmpSet);
        }
        return set;
    }

    //packageName的形式为xx.xx.xx.xx
    public static HashSet<Class> load(final String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        HashSet<Class> set = new HashSet<Class>();
        Enumeration<URL> urlEnumeration = classLoader.getResources(packageName);
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            //是xxx/xxx/的形式
            String currentPackagePath = url.getPath();
            String protocol = url.getProtocol();
            if (protocol.equals("jar")) {



            } else if (protocol.equals("file")) {
                File file = new File(currentPackagePath);
                //是文件夹，则扫描并加载该文件夹下面的类
                if (file.isDirectory()) {

                    File[] subFiles = file.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            if (pathname.getName().endsWith(".class")
                                    || pathname.getName().endsWith(".jar")) {
                                return true;
                            }
                            if (pathname.isDirectory()){
                                return true;
                            }
                            return false;
                        }
                    });
                    for (File subFile : subFiles) {
                        // xxx/xxx/xx/cax.class
                        String fileName = subFile.getName();
                        int index = fileName.lastIndexOf(".class");
                        if (index>0){
                            fileName = fileName.substring(0,index);
                            String subPackageName = packageName+"."+fileName;
                            set.add(doLoadClass(subPackageName,classLoader));
                        }else {
                            String subPackageName = packageName+"."+fileName;
                            set.addAll(load(subPackageName,classLoader));
                        }
                    }
                } else if (file.getName().endsWith(".class")) {
                    //是class，则加载该class
                    set.add(doLoadClass(packageName, classLoader));
                }
            }

        }
        return set;
    }


    private static HashSet<Class> doLoadJar(String packageName, ClassLoader classLoader) {
        HashSet<Class> set = new HashSet<Class>();


        return set;
    }


    private static Class doLoadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }


}
