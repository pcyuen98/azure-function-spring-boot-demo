
package com.belisty.function.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import com.share.FunctionMetric;

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
    void getYesterdayCostTest() {

        AtomicInteger attempts = new AtomicInteger(0);

        await()
                .alias("Azure yesterday cost management test")
                .pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(RETRY_DELAY_MS))
                .atMost(Duration.ofMinutes(10))
                .until(() -> {

                    int attempt = attempts.incrementAndGet();

                    log.info(
                            "Attempt {}/{} started",
                            attempt,
                            MAX_RETRIES
                    );

                    try {

                        Double yesterdayCost =
                                azureCostManagementService.getYesterdayPreTaxCost(
                                        azureCostManagementService.getYesterdayCostList()
                                );

                        assertNotNull(
                                yesterdayCost,
                                "yesterdayCost should not return null"
                        );

                        log.info(
                                "Yesterday cost: {}",
                                yesterdayCost
                        );

                        Double apiCount =
                                azureFunctionMetricService.getMetric(
                                        FunctionMetric.FUNCTION_EXECUTION_COUNT
                                );

                        assertNotNull(
                                apiCount,
                                "execution count should not return null"
                        );

                        log.info(
                                "API execution count: {}",
                                apiCount
                        );

                        azureCostManagementService.limitUpAndshutdown();

                        log.info(
                                "Test completed successfully on attempt {}",
                                attempt
                        );

                        return true;

                    } catch (Exception | AssertionError e) {

                        log.error(
                                "Attempt {}/{} failed: {}",
                                attempt,
                                MAX_RETRIES,
                                e.getMessage(),
                                e
                        );

                        if (attempt >= MAX_RETRIES) {
                            throw new AssertionError(
                                    "Test failed after "
                                            + MAX_RETRIES
                                            + " attempts",
                                    e
                            );
                        }

                        log.info(
                                "Waiting {} seconds before retry...",
                                RETRY_DELAY_MS / 1000
                        );

                        return false;
                    }
                });
    }
}
