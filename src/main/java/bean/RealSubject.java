package bean;

import anotation.Component;

/**
 * Created by zhangxj on 2017/3/29.
 */
@Component
public class RealSubject implements SubjectInterface {
    @Override
    public void show(String arg) {
        System.out.println("Hello "+ arg);
    }
}
