package com.belisty.function.service;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.util.function.service.AzureResourceManagerService;

import lombok.AllArgsConstructor;

/**
 * Azure Function using Spring Cloud Function style. This will be triggered via
 * FunctionInvoker.
 */
@Component
@AllArgsConstructor
public class AzureResourceManagerFunction {

	private final AzureResourceManagerService azureResourceManagerService;

	@Bean(name = "azureResourceManager")
	public Function<String, String> loggerTest() {
		return azureResourceManagerService::getAzureResourceSummary;
	}

	@Bean(name = "stopAzureFunction")
	public Function<String, String> stopAzureService() {
		return azureResourceManagerService::stopAzureService;
	}
}
