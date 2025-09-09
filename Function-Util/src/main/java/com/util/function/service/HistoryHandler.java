package com.util.function.service;

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

public class HistoryHandler extends FunctionInvoker<String, String> {

    @FunctionName("getHistory")
    public HttpResponseMessage getHistory(
            @HttpTrigger(name = "request",
                    methods = { HttpMethod.GET, HttpMethod.POST },
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        context.getLogger().info("Fetching last log records from Azure Table Storage");

        // Get "limit" parameter, default to 10
        int limit = Optional.ofNullable(request.getQueryParameters().get("limit"))
                .map(Integer::parseInt)
                .orElse(10);

        context.getLogger().info("Limit parameter: " + limit);

        // Pass limit as string to handleRequest
        String result = handleRequest(String.valueOf(limit), context);

        return ResponseEntityUtil.buildResponse(request, result);
    }
}
