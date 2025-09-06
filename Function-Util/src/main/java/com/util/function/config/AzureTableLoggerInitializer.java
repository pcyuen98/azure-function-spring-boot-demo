package com.util.function.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.util.function.service.AzureResourceManagerService;
import com.util.function.service.AzureTableLoggerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
//@AllArgsConstructor
public class AzureTableLoggerInitializer implements ApplicationRunner {

	private final AzureResourceManagerService azureResourceManagerService;
	
	private final AzureTableLoggerService loggerService;
	
	public final static String LOGGER_TABLE_NAME = "logger";

	public AzureTableLoggerInitializer(AzureTableLoggerService loggerService,
			AzureResourceManagerService azureResourceManagerService) {
		this.loggerService = loggerService;
		this.azureResourceManagerService = azureResourceManagerService;
	}

	@Override
	public void run(ApplicationArguments args) {
		azureResourceManagerService.initialize();

		loggerService.initialize(azureResourceManagerService.getStorageConnectionString(),
				AzureTableLoggerInitializer.LOGGER_TABLE_NAME);
		
		log.info("loggerService.log(\"INFO\", \"Azure Table Logger initialized successfully.\");");
		loggerService.log("INFO", "Azure Table Logger initialized successfully.");
	}
}
