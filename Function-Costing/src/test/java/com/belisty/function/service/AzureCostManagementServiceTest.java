package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ComponentScan(basePackages = { "com.util.function.service", "com.belisty.function.service" }

)
class AzureCostManagementServiceTest {

	@Autowired
	private AzureCostManagementService azureCostManagementService;

	@Test
	void sendEmail() throws IOException {
		log.info("sendEmail test starts");

		String result = azureCostManagementService.sendEmail();
		assertNotNull(result, "sendEmail should not return null");
		assertEquals("OK", result, "sendEmail should return 'OK' when successful");
	}

}
