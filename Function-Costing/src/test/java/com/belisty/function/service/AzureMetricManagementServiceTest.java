package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricValue;
import com.azure.monitor.query.models.MetricsQueryResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ComponentScan(basePackages = { "com.util.function.service", "com.belisty.function.service" })
class AzureMetricManagementServiceTest {

    @Value("${azure.function.resource-id}")
    private String resourceId;
    
    @Autowired
    private AzureCostManagementService azureCostManagementService;

    // Example unit test
    // @Test
    void sendEmail() throws IOException {
        log.info("sendEmail test starts");

        String result = azureCostManagementService.sendEmail();
        assertNotNull(result, "sendEmail should not return null");
        assertEquals("OK", result, "sendEmail should return 'OK' when successful");
    }

    // Example metrics query
    @Test
    void testMetricsQuery() {

        MetricsQueryClient client = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        MetricsQueryResult result = client.queryResource(
                resourceId,
                Arrays.asList("FunctionExecutionCount")
        );

        double totalSum = 0.0;

        for (MetricResult metric : result.getMetrics()) {
            System.out.println("Metric: " + metric.getMetricName());
            metric.getTimeSeries().forEach(ts -> {
                for (MetricValue value : ts.getValues()) {
                    System.out.println(value.getTimeStamp() + " -> " + value.getTotal());
                }
            });

            // Sum across all time series values
            totalSum += metric.getTimeSeries().stream()
                    .flatMap(ts -> ts.getValues().stream())
                    .filter(v -> v.getTotal() != null)
                    .mapToDouble(v -> v.getTotal())
                    .sum();
        }

        System.out.println("=== Total FunctionExecutionCount in queried interval ===");
        System.out.println("SUM = " + totalSum);
    }
}
