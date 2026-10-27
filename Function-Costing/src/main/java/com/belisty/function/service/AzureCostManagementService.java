package com.belisty.function.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
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
	 * Executes a cost query, serializes the result to JSON, and delegates sending
	 * the email.
	 *
	 * @param manager        The authenticated CostManagementManager.
	 * @param subscriptionId The Azure subscription ID.
	 */
	public List<Map<String, Object>> querySubscriptionCostLegacy(CostManagementManager manager, String subscriptionId) {
		Response<QueryResult> q = manager.queries()
				.usageWithResponse("subscriptions/" + subscriptionId,
						new QueryDefinition().withType(ExportType.ACTUAL_COST)
								.withTimeframe(TimeframeType.WEEK_TO_DATE)
								.withDataset(new QueryDataset().withGranularity(GranularityType.DAILY)
										.withAggregation(Map.of("totalCost",
												new QueryAggregation().withName("PreTaxCost") // Azure’s cost column
														.withFunction(FunctionType.SUM)))),
						com.azure.core.util.Context.NONE);

		QueryResult result = q.getValue();

		List<Map<String, Object>> mappedResults = result.rows().stream()
				.map(row -> java.util.stream.IntStream.range(0, result.columns().size()).boxed()
						.collect(java.util.stream.Collectors.toMap(i -> result.columns().get(i).name(), row::get)))
				.collect(java.util.stream.Collectors.toList());

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			String json = mapper.writeValueAsString(mappedResults);
			log.info("Cost Management Query Result JSON:\n{}", json);
		} catch (Exception e) {
			log.error("Error serializing QueryResult to JSON", e);
		}

		return mappedResults;

	}

	public List<Map<String, Object>> getYesterdayCostList() throws IOException {
		AzureResourceManager ar = azureResourceManagerService.getAzure();

		String subscriptionId = ar.subscriptionId();
		String tenantId = ar.tenantId(); // Get tenant ID for authentication
		log.info("Subscription ID: {}", subscriptionId);

		CostManagementManager costManager = CostManagementManager.authenticate(
				new DefaultAzureCredentialBuilder().build(),
				new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE));

		log.info("CostManagementManager initialized for subscription: {}", subscriptionId);

		return querySubscriptionCostLegacy(costManager, subscriptionId);
	}

	private String constructEmail() throws IOException {
		List<Map<String, Object>> mappedResults = getYesterdayCostList();

		
		List<String> recipients = new ArrayList<>();
		recipients.add(azureCostManagementProperties.getReceipientEmail());
		double totalCount = azureFunctionMetricService.getMetric(FunctionMetric.FUNCTION_EXECUTION_COUNT);
		EmailContent emailContent = AzureCostEmailBuilder.buildDailyCostEmail(totalCount, mappedResults);
		String subject = "(" + totalCount + "/hour) " + emailContent.getSubject();
		return emailService.sendEmail(recipients, subject, emailContent.getBody());
	}

	public Double getYesterdayPreTaxCost(List<Map<String, Object>> results) {
		// yesterday in UTC as int (yyyyMMdd)
		int yesterdayInt = Integer
				.parseInt(LocalDate.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

		return results.stream().filter(row -> Integer.parseInt(row.get("UsageDate").toString()) == yesterdayInt)
				.map(row -> row.get("PreTaxCost")).filter(Objects::nonNull)
				.mapToDouble(val -> Double.parseDouble(val.toString())).findFirst().orElse(0.0); // return 0.0 if not
																									// found
	}

	public String sendEmail() throws IOException {

		String emailStatus = null;
		try {
			emailStatus = this.constructEmail();
		} catch (Exception e) {
			this.azureTableLoggerService.log("INFO", e.getMessage());
		}

		return emailStatus;
	}

	public void limitUpAndshutdown() throws IOException {

		Double yesterdayCost = getYesterdayPreTaxCost(getYesterdayCostList());

		Double apiCount = azureFunctionMetricService.getMetric(FunctionMetric.FUNCTION_EXECUTION_COUNT);

		if (apiCount > azureCostManagementProperties.getApiLimit()
				|| yesterdayCost > azureCostManagementProperties.getCostLimit()) {
			FunctionApp functionApp = azureResourceManagerService
					.getFunctionAppById(azureCostManagementProperties.getResourceId());
			azureTableLoggerService.log("INFO",
					"Function limit up apiCount=" + apiCount + " yesterdayCost=" + yesterdayCost);
			sendEmail();
			functionApp.stop();
		}
	}
}