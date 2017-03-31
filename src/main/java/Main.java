import com.bean.ClassRoom;
import com.bean.Student;
import com.bean.SubjectInterface;
import fake.spring.BeanContext;

/**
 * Created by zhangxj on 2017/3/27.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        ClassRoom classRoom = BeanContext.getBean(ClassRoom.class);
        Student student = classRoom.getStudent();
        student.sayHello();
        SubjectInterface subjectInterface = BeanContext.getBean(SubjectInterface.class);
        System.out.println("\n");
        subjectInterface.show("everyone");
        System.out.println("\n");
        subjectInterface.saySomething();
    }
}
