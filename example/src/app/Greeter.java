package com.example.app;

import com.example.person.Person;
import com.example.time.TimeUtil;
import com.example.format.Formatter;

public class Greeter {
    public String greet(String name) {
        return greet(new Person(name));
    }

    public String greet(Person p) {
        return Formatter.formatGreeting(p.getName(), TimeUtil.nowString());
    }

    public static class Hello {
        public static void sayHello() {
            System.out.println("Hello");
        }
    }
}