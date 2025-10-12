package com.belisty.function.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class AzureCostManagementProperties {

	@Value("${azure.function.resource-id}")
	private String resourceId;

	@Value("${azure.function.receipient.email}")
	private String receipientEmail;

	// EngageLab API properties
	@Value("${belisty.engagelab.api.user}")
	private String engageLabApiUser;

	@Value("${belisty.engagelab.api.key}")
	private String engageLabApiKey;

	@Value("${belisty.engagelab.api.base-url}")
	private String engageLabApiBaseUrl;

	@Value("${belisty.engagelab.api.from-email}")
	private String engageLabApiFromEmail;

	// Function limit properties
	@Value("${azure.function.limit.cost}")
	private int costLimit;

	@Value("${azure.function.limit.api}")
	private int apiLimit;
}
