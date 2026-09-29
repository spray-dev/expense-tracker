package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.expensetracker.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ExpensetrackerapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensetrackerapiApplication.class, args);
	}

}
