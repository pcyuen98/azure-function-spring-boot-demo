package com.belisty.function.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.belisty.function.util.AzureCostManagementProperties;
import com.share.FunctionMetric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureFunctionMetricService {
	
	private final AzureCostManagementProperties azureCostManagementProperties;
	
    public double getMetric(FunctionMetric functionMetricName) {

        MetricsQueryClient client = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        MetricsQueryResult result = client.queryResource(
        		azureCostManagementProperties.getResourceId(),
                Arrays.asList(functionMetricName.getMetricName())
        );

        double totalSum = 0.0;

        for (MetricResult metric : result.getMetrics()) {
        	log.info("Metric: " + metric.getMetricName());
        	
        	metric.getTimeSeries().forEach(ts -> ts.getValues()
        		    .forEach(value -> log.info(value.getTimeStamp() + " -> " + value.getTotal())));

            // Sum across all time series values
            totalSum += metric.getTimeSeries().stream()
                    .flatMap(ts -> ts.getValues().stream())
                    .filter(v -> v.getTotal() != null)
                    .mapToDouble(v -> v.getTotal())
                    .sum();
        }
        
        return totalSum;
    }
        
}
