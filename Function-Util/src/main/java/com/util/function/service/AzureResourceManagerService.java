package com.util.function.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.Data;

@Data
@Service
public class AzureResourceManagerService {

	private AzureResourceManager azure;

	private com.azure.resourcemanager.storage.models.StorageAccount storageAccount;

	private final AzureTableLoggerService loggerService;

	@Value("${azure.resource.group}")
	private String rg;

	@Value("${azure.function.name}")
	private String functionName;

	@Value("${azure.storage.name}")
	public String storageName;

	public void initialize() {
		this.azure = AzureResourceManager
				.authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
				.withDefaultSubscription();

		this.storageAccount = this.azure.storageAccounts().getByResourceGroup(rg, storageName);
	}

	public String getStorageConnectionString() {
		List<StorageAccountKey> keys = storageAccount.getKeys();
		String accountKey = keys.get(0).value(); // pick primary key

		return String.format(
				"DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
				storageName, accountKey);

	}

	public String stopAzureService(String input) {
		FunctionApp functionApp = getAzure().functionApps().getByResourceGroup(getRg(), getFunctionName());
		loggerService.info("Stopping Azure Service");
		functionApp.stop();
		return "ok";
	}

	public String getAzureResourceSummary(String clientIp) {
		FunctionApp functionApp = getAzure().functionApps().getByResourceGroup(getRg(), getFunctionName());

		Map<String, String> map = new HashMap<>();
		if (clientIp != null && clientIp.contains(",")) {
			// Azure adds multiple IPs, first one is client
			clientIp = clientIp.split(",")[0].trim();
		}

		map.put("IPAdress", clientIp);
		map.put("ResourceGroupName", getRg());
		map.put("StorageAccountName", getStorageName());

		if (functionApp != null) {
			map.put("functionAppName Key", functionApp.key());
		}

		String json = null;
		try {
			ObjectWriter ow = new ObjectMapper().writerWithDefaultPrettyPrinter();
			json = ow.writeValueAsString(map);

		} catch (Exception e) {
			loggerService.info(e.getMessage());
		}
		return json; // return JSON string instead of raw name
	}
}
