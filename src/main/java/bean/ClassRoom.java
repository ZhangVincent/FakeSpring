package bean;

import fake.spring.anotation.Autowired;
import fake.spring.anotation.Component;

/**
 * Created by zhangxj on 2017/3/27.
 */
@Component
public class ClassRoom {

    @Autowired(name = "my_student")
    Student student;

    public Student getStudent(){
        return student;
    }

}
