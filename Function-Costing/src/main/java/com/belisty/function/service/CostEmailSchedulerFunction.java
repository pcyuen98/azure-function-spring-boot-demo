package com.belisty.function.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.util.function.service.AzureTableLoggerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CostEmailSchedulerFunction {

	private final AzureTableLoggerService loggerService;
	
	private final AzureCostManagementService azureCostManagementService;

	@FunctionName("keepAlive")
	public void keepAlive(@TimerTrigger(name = "keepAliveTrigger", schedule = "0 */60 * * * *") String timerInfo,
			ExecutionContext context) throws IOException {

		String result = azureCostManagementService.sendEmail();
		
		loggerService.log("INFO", "Scheduler works updated - " + result);
		
	}
	
	@FunctionName("shutdown")
	public void shutdown(@TimerTrigger(name = "shutdownTrigger", schedule = "0 */10 * * * *") String timerInfo,
			ExecutionContext context) throws IOException {

		azureCostManagementService.limitUpAndshutdown();
		
		loggerService.log("INFO", "Shutdown check invoked - ");
		loggerService.cleanupOldLogs(100);
		
	}
}
