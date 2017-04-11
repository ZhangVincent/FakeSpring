
> 工作之后，接触到的每个项目机会都会用到Spring框架。在经过一段时间初步学会如何使用Spring之后，为了更好的理解其IOC和AOP原理，在还没来得及看其源码的情况下（主要是感觉学习成本，尤其是时间成本会比较大），我决心按照自己的理解重复造轮子——FakeSpring，一款仿Spring的IOC和AOP框架。

我的博客地址:http://blog.csdn.net/yufengzxj
FakeSpring的具体介绍:http://blog.csdn.net/yufengzxj/article/details/68939015


使用方法：假设在com.bean下存在接口SubejectInterface和其实现类RealSubject，并定义了相应的切面和通知:
```java
public interface SubjectInterface {
    void show(String arg);
    void saySomething();
}

@Component
public class RealSubject implements SubjectInterface {
    @Override
    public void show(String arg) {
        System.out.println("Hello "+ arg);
    }

    @Override
    public void saySomething() {
        System.out.println("ok, i say something~~ i should have no advice~~");
    }
}

@Aspect
public class MyAspect {
    @Before(joinPoint = "com.bean.RealSubject.show.*")
    public void before(){
        System.out.println("----------------  before method  ----------------");
    }

    @After(joinPoint = "com.bean.RealSubject.show*")
    public void after(){
        System.out.println("----------------  after method  ----------------");
    }
}

使用如下：
SubjectInterface subjectInterface = BeanContext.getBean(SubjectInterface.class);
System.out.println("\n");
subjectInterface.show("everyone");
System.out.println("\n");
subjectInterface.saySomething();

输出如下:


----------------  before method  ----------------
Hello everyone
----------------  after method  ----------------


ok, i say something~~ i should have no advice~~
```
