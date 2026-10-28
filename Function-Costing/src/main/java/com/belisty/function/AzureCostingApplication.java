package com.belisty.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.util",
		"com.util.function.service",
		"com.belisty.function",
	})
public class AzureCostingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureCostingApplication.class, args);
    }
}
