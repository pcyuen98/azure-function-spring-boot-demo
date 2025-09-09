package com.util.function.service;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class HistoryFunction {

    private final AzureTableLoggerService loggerService;

    @Bean(name = "getHistory")
    public Function<Integer, String> getHistory() {
        return input -> {
            try {
            	loggerService.log("INFO", "Get history triggered");
                // Pass input parameter to loggerService
            	loggerService.cleanupOldLogs(50);
                return loggerService.getHistory(input != null ? input : 10); // Default to 10 if null
            } catch (JsonProcessingException e) {
            	loggerService.log("INFO", e.getCause().getMessage());
                e.printStackTrace();
                return "[]"; // return empty JSON on error
            }
        };
    }
}
