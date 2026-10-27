package com.belisty.function.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.share.FunctionMetric;
import com.util.function.service.AzureResourceManagerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ComponentScan(basePackages = {
        "com.util.function.service",
        "com.belisty.function.service"
})
class AzureYesterdayCostManagementServiceTest {

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 10_000;

    @Autowired
    private AzureCostManagementService azureCostManagementService;

    @Autowired
    private AzureFunctionMetricService azureFunctionMetricService;

    @Test
    void getYesterdayCostTest() throws IOException {

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            try {
                log.info("Attempt {}/{} started",
                        attempt, MAX_RETRIES);

                Double yesterdayCost =
                        azureCostManagementService.getYesterdayPreTaxCost(
                                azureCostManagementService.getYesterdayCostList()
                        );

                assertNotNull(
                        yesterdayCost,
                        "yesterdayCost should not return null"
                );

                log.info("Yesterday cost: {}", yesterdayCost);

                Double apiCount =
                        azureFunctionMetricService.getMetric(
                                FunctionMetric.FUNCTION_EXECUTION_COUNT
                        );

                assertNotNull(
                        apiCount,
                        "execution count should not return null"
                );

                log.info("API execution count: {}", apiCount);

                azureCostManagementService.limitUpAndshutdown();

                log.info("Test completed successfully on attempt {}",
                        attempt);

                return;

            } catch (Exception | AssertionError e) {

                lastException = e instanceof Exception
                        ? (Exception) e
                        : new RuntimeException(e);

                log.error(
                        "Attempt {}/{} failed: {}",
                        attempt,
                        MAX_RETRIES,
                        e.getMessage(),
                        e
                );

                if (attempt == MAX_RETRIES) {
                    break;
                }

                log.info(
                        "Waiting {} seconds before retry...",
                        RETRY_DELAY_MS / 1000
                );

                try {
                    Thread.sleep(RETRY_DELAY_MS);

                } catch (InterruptedException interruptedException) {

                    Thread.currentThread().interrupt();

                    fail("Retry interrupted", interruptedException);
                }
            }
        }

        fail(
                "Test failed after " + MAX_RETRIES + " attempts",
                lastException
        );
    }
}