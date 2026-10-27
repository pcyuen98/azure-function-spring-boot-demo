package com.belisty.function.service.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.belisty.function.util.AzureCostManagementProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ComponentScan(basePackages = {
    "com.util",
    "com.belisty.function.service.email"
})
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AzureCostManagementProperties azureCostManagementProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendEmail() throws IOException {

        List<String> recipients = new ArrayList<>();
        recipients.add(azureCostManagementProperties.getReceipientEmail());

        String result = emailService.sendEmail(
            recipients,
            "Hello Welcome to EngageLab Test",
            "Hi! Congrats on sending your first email."
        );

        log.info("EngageLab Response: {}", result);

        assertNotNull(result, "sendEmail should not return null");

        JsonNode json = objectMapper.readTree(result);

        assertTrue(
            json.has("email_ids"),
            "Response should contain email_ids"
        );

        JsonNode emailIds = json.get("email_ids");

        assertTrue(
            emailIds.isArray() && !emailIds.isEmpty(),
            "email_ids should be a non-empty array"
        );

        String emailId = emailIds.get(0).asText();

        assertFalse(
            emailId.isBlank(),
            "Email ID should not be empty"
        );

        log.info("Email accepted by EngageLab. Email ID: {}", emailId);
    }
}