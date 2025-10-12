package com.belisty.function.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = AzureCostManagementPropertiesTest.TestConfig.class)

class AzureCostManagementPropertiesTest {

	@Autowired
	private Environment env;

	@Autowired
	private AzureCostManagementProperties props;

	@Configuration
	@EnableConfigurationProperties(AzureCostManagementProperties.class)
	@Import(AzureCostManagementProperties.class) // Explicitly import the bean to load
	static class TestConfig {
		// This minimal configuration loads only the required
		// AzureCostManagementProperties bean.
	}

	@Test
	void shouldLoadValuesFromApplicationProperties() {

		log.info("Active Profiles: " + Arrays.toString(env.getActiveProfiles()));
		log.info("Default Profiles: " + Arrays.toString(env.getDefaultProfiles()));

		log.info("=== AzureCostManagementProperties (from @TestPropertySource) ===");
		log.info("Resource ID:           " + props.getResourceId());
		log.info("Recipient Email:       " + props.getReceipientEmail());
		log.info("EngageLab API User:    " + props.getEngageLabApiUser());
		log.info("EngageLab API Key:     " + props.getEngageLabApiKey());
		log.info("EngageLab Base URL:    " + props.getEngageLabApiBaseUrl());
		log.info("EngageLab From Email:  " + props.getEngageLabApiFromEmail());
		log.info("Cost Limit:            " + props.getCostLimit());
		log.info("API Limit:             " + props.getApiLimit());
		log.info("===================================================================");

		// ✅ Assertions to verify the values loaded from @TestPropertySource
		assertThat(props.getResourceId()).isNotNull();
		String azureResourceIdRegex = "^/subscriptions/[0-9a-fA-F\\-]+/resourceGroups/[A-Za-z0-9._\\-()]+/providers/Microsoft\\.Web/sites/[A-Za-z0-9._\\-()]+$";

		assertThat(props.getResourceId()).as("Azure Function resource ID should follow the correct Azure format")
				.matches(azureResourceIdRegex);

		assertThat(props.getReceipientEmail()).isNotNull();
		assertThat(props.getEngageLabApiUser()).isNotNull();
		assertThat(props.getEngageLabApiKey()).isNotNull();
		assertThat(props.getEngageLabApiBaseUrl())
				.withFailMessage("EMail API Base URL must use HTTPS: %s", props.getEngageLabApiBaseUrl())
				.startsWith("https://"); // The assertion predicate is now last

		assertThat(props.getEngageLabApiBaseUrl()).isNotNull();
		assertThat(props.getCostLimit()).isGreaterThan(0);
		assertThat(props.getApiLimit()).isGreaterThan(0);
	}
}
