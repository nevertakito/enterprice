package com.example.lab3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CustomerManagement {

	public static void main(String[] args) {
		SpringApplication.run(CustomerManagement.class, args);
	}

}
