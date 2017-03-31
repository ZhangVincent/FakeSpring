
> 工作之后，接触到的每个项目机会都会用到Spring框架。在经过一段时间初步学会如何使用Spring之后，为了更好的理解其IOC和AOP原理，在还没来得及看其源码的情况下（主要是感觉学习成本，尤其是时间成本会比较大），我决心按照自己的理解重复造轮子——FakeSpring，一款仿Spring的IOC和AOP框架。

FakeSpring的Github地址为:https://github.com/ZhangVincent/FakeSpring

我的博客地址:http://blog.csdn.net/yufengzxj

## IOC
IOC，控制反转，就是将原先由程序源代码管理对象的权限交给容器，由容器来负责对象的组装和管理。通俗来讲，就是在编程的时候，你不需要在自己的代码中直接或间接的调用类的构造方法来生成对象，而是容器已经把对象给你生成好了，你只需要从容器中直接拿取就行。

我们通过以下方法可以达到IOC的目的：（1）将我们需要的类信息加载到JVM；（2）利用反射，对每一个类生成相应的实例；（3）对于实例中需要注入值的字段，我们从已有的实例中找到对应的实例，将其赋值给字段。

整个过程如以下BeanContext类的static静态代码块所示：
```java
public class BeanContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanContext.class);
    private static final String CLASS_PATH = BeanContext.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    private static final Map<Class, Object> clsToObject = new HashMap<>();
    private static final Map<String, Object> nameToObject = new HashMap<>();
    
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
                if (cls.isAnnotationPresent(Component.class)) {
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
        LOGGER.info("Bean Context is already to be used!!!");
    }
    public static <T> T getBean(String beanName, Class<T> cls) {
        return (T) nameToObject.get(beanName);
    }

    public static <T> T getBean(Class<T> cls) {
        return (T) clsToObject.get(cls);
    }
}

```
在FakeSpring中，我们定了Component注解和Autowired注解，其作用与Spring中Component和Autowired注解一致。
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String name() default "";
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    String name() default "";
}
```
同时，在代码中，ClassUtil是一个会递归的加载指定包下面的class文件的类：
```java
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
```
我们还定义了ReflectionUtil类，该类负责反射的处理，包括实例的生成、给字段设值、调用方法。
```java
public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);


    public static Object instanceObject(Class cls) {
        Object object = null;
        try {
            object = cls.newInstance();
        } catch (Exception e) {
            LOGGER.error("class instance failed:\n" + e.getMessage());
        }
        return object;
    }

    public static void setField(Object targetObject, Field field, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(targetObject, fieldValue);
        } catch (IllegalAccessException e) {
            LOGGER.error("set field failed:\n" + e.getMessage());
        }
    }

    public static Object invokeMethod(Object targetObject, Method method, Object... args) {
        Object result = null;
        try {
            method.setAccessible(true);
            result = method.invoke(targetObject, args);
        } catch (Exception e) {
            LOGGER.error("invoke method failed:\n" + e.getMessage());
        }
        return result;
    }
}
```
实现IOC的整个过程如上：BeanContext是容器，扫描整个项目下的类，对被Component修饰的类生成bean，对bean内部被Autowired修饰的字段赋值（值为已经生成的bean）。

使用方法如下（假设com.bean包下面有Classroom类和Student类）:
```java
@Component(name = "my_student")
public class Student {
    String name="Tom";
    int age = 18;

    public Student(){}
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void sayHello(){
        System.out.println("Hi, I am "+name+" and i am "+age+" years old");
    }
}

@Component
public class ClassRoom {

    @Autowired(name = "my_student")
    Student student;

    public Student getStudent(){
        return student;
    }

}


调用：
Classroom classroom = BeanContext.getBean(Classroom.class);
Student student = classRoom.getStudent();
//classroom = BeanContext.getBean("my_student",Student.class);
student.sayHello();
```

## AOP
FakeSpring的AOP功能，是利用java的动态代理来实现的。关于java动态代理的使用和原理，这两篇文章讲的比较透彻：http://www.cnblogs.com/xiaoluo501395377/p/3383130.html 和  http://www.cnblogs.com/flyoung2008/archive/2013/08/11/3251148.html 。
结合java的动态代理，为了使AOP发挥作用，我们的设想是：如果对类中的某个方法声明了前置通知或者后置通知的话，我们利用该类的bean生成InvocationHandler的实例，并在InvocationHandler实例中保存前后置通知信息，用InvocationHandler实例生成的代理对象替换BeanContext中的bean。同时，我们在InvocationHandler的invoke方法中，在连接点被调用前后，调用相应的前置通知和后置通知。
首先，我们定义三个注解：被Aspect注解修饰的类是一个切面类；切面类中有被After和Before注解修饰的方法，为后置通知和前置通知；Before和After注解中joinPoint的值定义了连接点。
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aspect {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Before {
    String joinPoint();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface After {
    String joinPoint();
}
```
其次，我们声明了一个实现了InvocationHandlder接口的类：
```java
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

```
然后，我们修改BeanContext，在适当的时候用动态代理对象替换bean对象:
```java
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

```
同时，为了判断某个通知是否是某个方法的前后置通知，我们定义了AOPUtil类，内部简单的用正则表达式来进行判断:
```java
public class AOPUtil {

    public static boolean isAdvice(Class targetClass, Method advice) {

        Method[] methods = targetClass.getMethods();
        for (Method method:methods){
            if (isBeforeAdvice(method,advice)||isAfterAdvice(method,advice)){
                return true;
            }
        }
        return false;
    }


    //判断是否是前置通知
    public static boolean isBeforeAdvice(Method joinPoint, Method advice) {
        if (advice.isAnnotationPresent(Before.class)) {
            String adviceJoinPointStr = advice.getAnnotation(Before.class).joinPoint();
            String joinPointStr = joinPoint.getDeclaringClass().getName() + "." + joinPoint.getName();
            Pattern p = Pattern.compile(adviceJoinPointStr);
            Matcher m = p.matcher(joinPointStr);
            return m.matches();
        }
        return false;
    }

    //判断是否是后置通知
    public static boolean isAfterAdvice(Method joinPoint, Method advice) {
        if (advice.isAnnotationPresent(After.class)) {
            String adviceJoinPointStr = advice.getAnnotation(After.class).joinPoint();
            String joinPointStr = joinPoint.getDeclaringClass().getName() + "." + joinPoint.getName();
            Pattern p = Pattern.compile(adviceJoinPointStr);
            Matcher m = p.matcher(joinPointStr);
            return m.matches();
        }
        return false;
    }
}
```
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

至此，初步实现了IOC和AOP的功能。当然，FakeSpring中也还存在很多可以改进的地方：
1. FakeSpring是基于java的动态代理实现的AOP功能，因此只能对接口的实现类进行前置和后置通知。这一块可以通过CGLib来改进，达到给任意类实现前后置通知的效果。
2. 这里只对前后置通知进行了处理，实际上还可以添加诸如方法抛出异常时候的通知，这需要声明对应的注解并在InvocationHandler的invoke方法里面添加逻辑。
3. 后续还可以有FakeSpringMVC，大致思路是：在容器中记录每种请求路径及其对应的(controller,method)，当外界向某条路径提交请求时，就执行controller的method，并返回执行结果。

