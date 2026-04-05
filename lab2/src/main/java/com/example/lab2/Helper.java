package com.example.lab2;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Helper {
    public static void main(String[] args) {
        String rawPassword = "admin123";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println(hashed);
    }
}