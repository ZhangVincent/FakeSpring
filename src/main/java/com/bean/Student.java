package com.bean;

import fake.spring.anotation.Component;

/**
 * Created by zhangxj on 2017/3/27.
 */
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
