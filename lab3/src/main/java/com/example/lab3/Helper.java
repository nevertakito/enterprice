package com.example.lab3;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Helper {
    public static void main(String[] args) {
        String rawPassword = "admin";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println(hashed);
    }
}