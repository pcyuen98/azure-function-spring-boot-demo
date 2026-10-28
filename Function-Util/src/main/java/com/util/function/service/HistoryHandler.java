package com.util.function.service;

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
public class HistoryHandler {

    private final Function<Integer, String> historyFunction;

    public HistoryHandler(@Qualifier("getHistory") Function<Integer, String> historyFunction) {
        this.historyFunction = historyFunction;
    }

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
        String result = historyFunction.apply(limit);

        return ResponseEntityUtil.buildResponse(request, result);
    }
}
