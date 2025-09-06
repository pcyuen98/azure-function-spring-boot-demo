package com.util.function.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class AzureResourceManagerServiceTest {

	@Autowired
	private AzureResourceManagerService azureResourceManagerService;

	@Test
	void azureResourceTest() {
		String json = azureResourceManagerService.getAzureResourceSummary("unknown IP");

		assertNotNull(json, "Azure resource summary JSON should not be null");
	}

	@Test
	void stopAzureService() {
		// Note: Only to shutdown service test
		// String result = azureResourceManagerService.stopAzureService(null);
		// assertNotNull(result, "Azure Function return should not be null");
	}
}
