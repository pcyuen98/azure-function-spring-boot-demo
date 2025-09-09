package com.belisty.function.service;

import java.util.Optional;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import com.demo.utility_classes.ResponseEntityUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class AzureResourceManagerHandler extends FunctionInvoker<String, String> {

	@FunctionName("azureResourceManager")
	public HttpResponseMessage azureResourceManager(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {
		
		String clientIp = request.getHeaders().getOrDefault("x-forwarded-for", "");
		return ResponseEntityUtil.buildResponse(request, handleRequest(clientIp, context));
	}

	@FunctionName("stopAzureFunction")
	public HttpResponseMessage stopAzureFunction(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {
		
		String clientIp = request.getHeaders().getOrDefault("x-forwarded-for", "");
		return ResponseEntityUtil.buildResponse(request, handleRequest(clientIp, context));
	}

}
