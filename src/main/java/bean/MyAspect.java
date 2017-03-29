package bean;

import fake.spring.anotation.After;
import fake.spring.anotation.Aspect;
import fake.spring.anotation.Before;

/**
 * Created by zhangxj on 2017/3/29.
 */
@Aspect
public class MyAspect {
    @Before(joinPoint = "bean.RealSubject.*")
    public void before(){
        System.out.println("----------------  before method  ----------------");
    }

    @After(joinPoint = "bean.RealSubject.*")
    public void after(){
        System.out.println("----------------  after method  ----------------");
    }
}
