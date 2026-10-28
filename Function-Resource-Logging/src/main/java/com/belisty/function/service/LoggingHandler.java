package com.belisty.function.service;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

@Component
public class LoggingHandler {

    private final Function<Void, String> loggingFunction;

    public LoggingHandler(@Qualifier("loggingtest") Function<Void, String> loggingFunction) {
        this.loggingFunction = loggingFunction;
    }
	
    @FunctionName("loggingtest")
    public HttpResponseMessage logTest(
            @HttpTrigger(name = "request", methods = {HttpMethod.GET, HttpMethod.POST},
                         authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .body(loggingFunction.apply(null))
                .build();
    }
}
