
package com.belisty.function.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryResult;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;

import com.belisty.function.service.email.EmailService;
import com.belisty.function.util.AzureCostEmailBuilder;
import com.belisty.function.util.AzureCostEmailBuilder.EmailContent;
import com.belisty.function.util.AzureCostManagementProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.share.FunctionMetric;
import com.util.function.service.AzureResourceManagerService;
import com.util.function.service.AzureTableLoggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureCostManagementService {

	private final AzureResourceManagerService azureResourceManagerService;

	private final AzureFunctionMetricService azureFunctionMetricService;

	private final AzureTableLoggerService azureTableLoggerService;

	private final EmailService emailService;

	private final AzureCostManagementProperties azureCostManagementProperties;

	/**
	 * Executes an Azure Cost Management query.
	 *
	 * Handles HTTP 429 rate-limit errors by logging available Azure quota and retry
	 * headers.
	 *
	 * @param manager        Authenticated CostManagementManager
	 * @param subscriptionId Azure subscription ID
	 * @return List of cost records
	 */
	public List<Map<String, Object>> querySubscriptionCostLegacy(CostManagementManager manager, String subscriptionId) {

		try {

			Response<QueryResult> q = manager.queries()
					.usageWithResponse("subscriptions/" + subscriptionId,
							new QueryDefinition().withType(ExportType.ACTUAL_COST)
									.withTimeframe(TimeframeType.WEEK_TO_DATE)
									.withDataset(new QueryDataset().withGranularity(GranularityType.DAILY)
											.withAggregation(Map.of("totalCost", new QueryAggregation()
													.withName("PreTaxCost").withFunction(FunctionType.SUM)))),
							Context.NONE);

			QueryResult result = q.getValue();

			List<Map<String, Object>> mappedResults = result.rows().stream()
					.map((List<Object> row) -> IntStream.range(0, result.columns().size()).boxed().collect(
							Collectors.toMap((Integer i) -> result.columns().get(i).name(), (Integer i) -> row.get(i))))
					.collect(Collectors.toList());

			try {

				ObjectMapper mapper = new ObjectMapper();
				mapper.enable(SerializationFeature.INDENT_OUTPUT);

				String json = mapper.writeValueAsString(mappedResults);

				log.info("Cost Management Query Result JSON:\n{}", json);

			} catch (Exception e) {

				log.error("Error serializing QueryResult to JSON", e);
			}

			return mappedResults;

		} catch (ManagementException e) {

			if (e.getResponse() != null && e.getResponse().getStatusCode() == 429) {

				HttpHeaders headers = e.getResponse().getHeaders();

				log.error("Azure Cost Management API rate limit exceeded (HTTP 429)");

				log.error("Remaining subscription reads: {}",
						getHeaderValue(headers, "x-ms-ratelimit-remaining-subscription-reads"));

				log.error("Cost Management entity quota: {}",
						getHeaderValue(headers, "x-ms-ratelimit-microsoft.costmanagement-entity-requests"));

				log.error("Retry-After: {}", getHeaderValue(headers, "Retry-After"));

				log.error("Azure response: {}", e.getMessage());

				// Do not return an empty list for rate-limit failures.
				// Propagate the exception so callers can retry or fail safely.
				throw e;
			}

			throw e;
		}
	}

	/**
	 * Safely retrieves an HTTP header value without using the deprecated
	 * HttpHeaders.getValue(String) method.
	 *
	 * @param headers HTTP response headers
	 * @param name    Header name
	 * @return Header value or N/A
	 */
	public static String getHeaderValue(HttpHeaders headers, String name) {

		HttpHeader header = headers == null ? null : headers.get(name);

		return header == null ? "N/A" : header.getValue();
	}

	/**
	 * Returns the last five characters of a string.
	 *
	 * @param input Input string
	 * @return Last five characters
	 */
	public static String getLast5Characters(String input) {

		if (input == null || input.length() < 5) {

			throw new IllegalArgumentException("String must contain at least 5 characters");
		}

		return input.substring(input.length() - 5);
	}

	/**
	 * Retrieves Azure Cost Management records.
	 *
	 * @return Cost records
	 * @throws IOException If an I/O error occurs
	 */
	public List<Map<String, Object>> getYesterdayCostList() throws IOException {

		AzureResourceManager ar = azureResourceManagerService.getAzure();

		String subscriptionId = ar.subscriptionId();

		String tenantId = ar.tenantId();

		log.info("Subscription ID: {}", subscriptionId);

		CostManagementManager costManager = CostManagementManager.authenticate(
				new DefaultAzureCredentialBuilder().build(),
				new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE));

		log.info("CostManagementManager initialized for subscription: {}", subscriptionId);

		return querySubscriptionCostLegacy(costManager, subscriptionId);
	}

	/**
	 * Builds and sends the daily Azure cost email.
	 *
	 * @return Email service response
	 * @throws IOException If an I/O error occurs
	 */
	private String constructEmail() throws IOException {

		List<Map<String, Object>> mappedResults = getYesterdayCostList();

		List<String> recipients = new ArrayList<>();

		recipients.add(azureCostManagementProperties.getReceipientEmail());

		double totalCount = azureFunctionMetricService.getMetric(FunctionMetric.FUNCTION_EXECUTION_COUNT);

		EmailContent emailContent = AzureCostEmailBuilder.buildDailyCostEmail(totalCount, mappedResults);

		String subject = getLast5Characters(azureCostManagementProperties.getResourceId()) + " (" + totalCount
				+ "/hour) " + emailContent.getSubject();

		return emailService.sendEmail(recipients, subject, emailContent.getBody());
	}

	/**
	 * Retrieves yesterday's PreTaxCost from Azure cost records.
	 *
	 * Yesterday is calculated in UTC.
	 *
	 * @param results Azure cost query results
	 * @return Yesterday's cost or 0.0 if no matching record exists
	 */
	public Double getYesterdayPreTaxCost(List<Map<String, Object>> results) {

		int yesterdayInt = Integer
				.parseInt(LocalDate.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

		return results.stream().filter(row -> Integer.parseInt(row.get("UsageDate").toString()) == yesterdayInt)
				.map(row -> row.get("PreTaxCost")).filter(Objects::nonNull)
				.mapToDouble(val -> Double.parseDouble(val.toString())).findFirst().orElse(0.0);
	}

	/**
	 * Sends the daily cost email.
	 *
	 * @return Email status
	 * @throws IOException If an I/O error occurs
	 */
	public String sendEmail() throws IOException {

		String emailStatus = null;

		try {

			emailStatus = this.constructEmail();

		} catch (Exception e) {

			log.error("Failed to send Azure cost email", e);

			this.azureTableLoggerService.log("INFO", e.getMessage());
		}

		return emailStatus;
	}

	/**
	 * Stops the Azure Function App when either:
	 *
	 * 1. API execution count exceeds the configured limit. 2. Yesterday's Azure
	 * cost exceeds the configured cost limit.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public void limitUpAndshutdown() throws IOException {

		Double yesterdayCost = getYesterdayPreTaxCost(getYesterdayCostList());

		Double apiCount = azureFunctionMetricService.getMetric(FunctionMetric.FUNCTION_EXECUTION_COUNT);

		log.info("Azure Function usage: apiCount={}, yesterdayCost={}", apiCount, yesterdayCost);

		if (apiCount > azureCostManagementProperties.getApiLimit()
				|| yesterdayCost > azureCostManagementProperties.getCostLimit()) {

			FunctionApp functionApp = azureResourceManagerService
					.getFunctionAppById(azureCostManagementProperties.getResourceId());

			azureTableLoggerService.log("INFO",
					"Function limit up apiCount=" + apiCount + " yesterdayCost=" + yesterdayCost);

			sendEmail();

			functionApp.stop();

			log.warn("Azure Function App stopped because usage limit was exceeded.");
		}
	}
}
