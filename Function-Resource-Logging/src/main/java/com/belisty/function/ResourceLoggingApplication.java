package com.belisty.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.util",
		"com.belisty.function",
	})
public class ResourceLoggingApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ResourceLoggingApplication.class, args);
    }
}
