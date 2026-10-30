
package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
@ComponentScan(basePackages = {
        "com.util.function.service",
        "com.belisty.function.service"
})
class AzureMetricManagementServiceTest {

    @Value("${azure.function.resource-id}")
    private String resourceId;

    @Autowired
    private AzureCostManagementService azureCostManagementService;

    // Optional email integration test
    // @Test
    void sendEmail() throws IOException {

        log.info("sendEmail test starts");

        String result = azureCostManagementService.sendEmail("AzureMetricManagementServiceTest UNIT TEST");

        assertNotNull(result, "sendEmail should not return null");

        assertEquals(
                "OK",
                result,
                "sendEmail should return 'OK' when successful"
        );
    }

    @Test
    void testMetricsQuery() {

        assertNotNull(
                resourceId,
                "Azure Function resource ID must not be null"
        );

        assertFalse(
                resourceId.isBlank(),
                "Azure Function resource ID must not be blank"
        );

        MetricsQueryClient client = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        MetricsQueryResult result = client.queryResource(
                resourceId,
                Arrays.asList("FunctionExecutionCount")
        );

        // SonarQube fix: Assert Azure query response
        assertNotNull(
                result,
                "Azure Metrics query result must not be null"
        );

        assertNotNull(
                result.getMetrics(),
                "Azure Metrics collection must not be null"
        );

        assertFalse(
                result.getMetrics().isEmpty(),
                "Azure Metrics collection must not be empty"
        );

        double totalSum = 0.0;

        for (MetricResult metric : result.getMetrics()) {

            assertNotNull(
                    metric,
                    "Metric result must not be null"
            );

            assertNotNull(
                    metric.getMetricName(),
                    "Metric name must not be null"
            );

            log.info("Metric: {}", metric.getMetricName());

            assertNotNull(
                    metric.getTimeSeries(),
                    "Metric time series must not be null"
            );

            metric.getTimeSeries().forEach(ts -> {

                assertNotNull(
                        ts.getValues(),
                        "Metric values must not be null"
                );

                for (MetricValue value : ts.getValues()) {

                    log.info(
                            "{} -> {}",
                            value.getTimeStamp(),
                            value.getTotal()
                    );
                }
            });

            totalSum += metric.getTimeSeries()
                    .stream()
                    .flatMap(ts -> ts.getValues().stream())
                    .filter(v -> v.getTotal() != null)
                    .mapToDouble(MetricValue::getTotal)
                    .sum();
        }

        log.info(
                "=== Total FunctionExecutionCount in queried interval ==="
        );

        log.info("SUM = {}", totalSum);

        // SonarQube fix: Assert valid execution count
        assertTrue(
                Double.isFinite(totalSum),
                "Total FunctionExecutionCount must be finite"
        );

        assertTrue(
                totalSum >= 0.0,
                "Total FunctionExecutionCount must not be negative"
        );
    }
}
