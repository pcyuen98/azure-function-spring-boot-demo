package com.belisty.function.service;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.demo.utility_classes.ResponseEntityUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

@Component
public class AzureResourceManagerHandler {

	private final Function<String, String> azureResourceManagerFunction;
	private final Function<String, String> stopAzureFunction;

	public AzureResourceManagerHandler(
			@Qualifier("azureResourceManager") Function<String, String> azureResourceManagerFunction,
			@Qualifier("stopAzureFunction") Function<String, String> stopAzureFunction) {
		this.azureResourceManagerFunction = azureResourceManagerFunction;
		this.stopAzureFunction = stopAzureFunction;
	}

	@FunctionName("azureResourceManager")
	public HttpResponseMessage azureResourceManager(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {
		
		String clientIp = request.getHeaders().getOrDefault("x-forwarded-for", "");
		return ResponseEntityUtil.buildResponse(request, azureResourceManagerFunction.apply(clientIp));
	}

	@FunctionName("stopAzureFunction")
	public HttpResponseMessage stopAzureFunction(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {
		
		String clientIp = request.getHeaders().getOrDefault("x-forwarded-for", "");
		return ResponseEntityUtil.buildResponse(request, stopAzureFunction.apply(clientIp));
	}

}
