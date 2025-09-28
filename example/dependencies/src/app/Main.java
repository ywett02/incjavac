package com.example.app;

import com.example.person.Person;

public class Main {
    public static void main(String[] args) {
        Greeter greeter = new Greeter();
        String message = greeter.greet(new Person("Iveta!!fdffdffdfsdffdf"));
        System.out.println(message);
    }
}