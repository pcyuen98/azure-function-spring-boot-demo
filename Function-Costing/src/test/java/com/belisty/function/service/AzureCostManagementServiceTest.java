package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class AzureCostManagementServiceTest {

    @Autowired
    private AzureCostManagementService azureCostManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendEmail() throws IOException {

        log.info("sendEmail test starts");

        String result = azureCostManagementService.sendEmail();

        assertNotNull(result, "Response should not be null");

        log.info("EngageLab Response: {}", result);

        JsonNode json = objectMapper.readTree(result);

        assertTrue(
            json.has("email_ids"),
            "Response should contain email_ids"
        );

        assertTrue(
            json.get("email_ids").isArray(),
            "email_ids should be an array"
        );

        assertFalse(
            json.get("email_ids").isEmpty(),
            "email_ids should not be empty"
        );

        log.info("Email accepted by EngageLab");
    }
}