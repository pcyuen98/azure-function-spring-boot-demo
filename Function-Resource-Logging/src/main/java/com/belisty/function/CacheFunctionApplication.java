package com.belisty.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.util",
		"com.belisty",
	})
public class CacheFunctionApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(CacheFunctionApplication.class, args);
    }
}
