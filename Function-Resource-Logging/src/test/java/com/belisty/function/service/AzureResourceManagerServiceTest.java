package com.belisty.function.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class AzureResourceManagerServiceTest {

	@Autowired
	private AzureResourceManagerService azureResourceManagerService;

	@Autowired
	private com.azure.resourcemanager.costmanagement.CostManagementManager costManagementManager;
	
	@Test
	void azureResourceSummaryTest() {
		//String json = azureResourceManagerService.getAzureResourceSummary("unknown IP");
		
		//assertNotNull(json, "Azure resource summary JSON should not be null");
		System.out.println("test");
		this.getDailyCost();
	}
	
    public Double getDailyCost() {
        QueryDefinition query = new QueryDefinition();

        AzureResourceManager azure = azureResourceManagerService.getAzure();
        
        QueryResult result = costManagementManager
                .queries()
                .usageWithResponse("subscriptions/" + azure.subscriptionId(), query, null)
                .getValue();

        if (result.rows().isEmpty()) {
            return 0.0;
        }

        // First row, first column = total cost
        return Double.parseDouble(result.rows().get(0).get(0).toString());
    }
	@Test
	void stopAzureService() {
	//	String result = azureResourceManagerService.stopAzureService(null);
	//	assertNotNull(result, "Azure Function return should not be null");
	}
}
