import bean.ClassRoom;
import bean.Student;
import utils.BeanContext;
import utils.ClassUtil;

import java.util.Set;

/**
 * Created by zhangxj on 2017/3/27.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        ClassRoom classRoom = BeanContext.getBean(ClassRoom.class);
        Student student = classRoom.getStudent();
        student.sayHello();
    }
}
