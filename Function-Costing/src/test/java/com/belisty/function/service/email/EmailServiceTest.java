package com.belisty.function.service.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.belisty.function.util.AzureCostManagementProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ComponentScan(basePackages = {"com.util", "com.belisty.function.service.email"})
class EmailServiceTest {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private AzureCostManagementProperties azureCostManagementProperties;
	
	@Test
	void sendEmail() throws IOException {
		
        List<String> recipients = new ArrayList<>();
        recipients.add(azureCostManagementProperties.getReceipientEmail());
        
        String result = emailService.sendEmail(recipients, "Welcome to EngageLab Test", "Hi！Congrats on sending your first email.");
        assertNotNull(result, "sendEmail should not return null");
        assertEquals("OK", result, "sendEmail should return 'OK' when successful");
	}

}
