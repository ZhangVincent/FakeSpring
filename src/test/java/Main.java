import utils.ClassUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by vincent on 21/03/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> e = classLoader.getResources("bean.bean1");
        while (e.hasMoreElements()){
            URL url =e.nextElement();
            System.out.println(url.getPath());
        }



        /*HashSet set = ClassUtil.load(new String[]{"bean"},classLoader);

        Iterator<Class> iterator = set.iterator();
        while (iterator.hasNext()){
            Class cls = iterator.next();
            System.out.println(cls);
        }*/

    }
}
