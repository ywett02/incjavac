package com.example.time;

import java.time.LocalTime;

public class TimeUtil {
    public static String nowString() {
        return LocalTime.now().withNano(0).toString();
    }
}