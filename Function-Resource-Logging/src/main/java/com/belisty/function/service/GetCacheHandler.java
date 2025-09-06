package com.belisty.function.service;

import java.util.Optional;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import com.belisty.function.model.GetCache;
import com.demo.utility_classes.ResponseEntityUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class GetCacheHandler extends FunctionInvoker<GetCache, String> {

	@FunctionName("putCache")
	public HttpResponseMessage putCache(
			@HttpTrigger(name = "request", methods = { HttpMethod.GET,
					HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {

		GetCache cache = new GetCache();
		cache.setData(request.getBody().orElseThrow(
			    () -> new IllegalArgumentException("Request body is missing")));

		return ResponseEntityUtil.buildResponse(request, handleRequest(cache, context));
	}

	@FunctionName("getCache")
	public HttpResponseMessage getCache(
			@HttpTrigger(name = "request", methods = { HttpMethod.GET,
					HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {

		GetCache cache = new GetCache();
		cache.setData("ok cache");

		return ResponseEntityUtil.buildResponse(request, handleRequest(cache, context));
	}

}
