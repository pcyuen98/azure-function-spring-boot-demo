package com.belisty.function.service;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

/**
 * Azure Function using Spring Cloud Function style. This will be triggered via
 * FunctionInvoker.
 */
@Component
@AllArgsConstructor
public class LoggerFunction {

	private final AzureTableLoggerService loggerService;

	@Bean(name = "loggingtest")
	public Function<Void, String> execute() {
		return input -> {
			
			String lastLogger = null;
			try {
				loggerService.info("loggerTest here");
				lastLogger =  loggerService.getHistory(1);
			} catch (Exception e) {
				loggerService.info(e.getMessage());
			}
			return lastLogger;
		};
	}

}
