package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.share.FunctionMetric;
import com.util.function.service.AzureResourceManagerService;

@SpringBootTest
@ComponentScan(basePackages = { "com.util.function.service", "com.belisty.function.service" })
class AzureYesterdayCostManagementServiceTest {
	@Autowired
	AzureResourceManagerService azureResourceManagerService;
	
	@Autowired
	private AzureCostManagementService azureCostManagementService;
	
	@Autowired 
	private AzureFunctionMetricService azureFunctionMetricService;

	@Test
	void getYesterdayCostTest() throws IOException {
		Double yesterdayCost = azureCostManagementService.getYesterdayPreTaxCost(azureCostManagementService.getYesterdayCostList());
		
		assertNotNull(yesterdayCost, "yesterdayCost should not return null");
		
		Double apiCount = azureFunctionMetricService.getMetric(FunctionMetric.FUNCTION_EXECUTION_COUNT);
		
		assertNotNull(apiCount, "execution count should not return null");
		
		azureCostManagementService.limitUpAndshutdown();

	}
	


    
 

}
