
package com.util.function.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryFunctionTest {

    @Mock
    private AzureTableLoggerService loggerService;

    private HistoryFunction historyFunction;

    private Function<Integer, String> getHistory;

    @BeforeEach
    void setUp() {
        historyFunction = new HistoryFunction(loggerService);
        getHistory = historyFunction.getHistory();
    }

    @Test
    void testGetHistoryWithValidInput() throws Exception {
        String expected = "[{\"message\":\"Test log\"}]";

        when(loggerService.getHistory(5)).thenReturn(expected);

        String result = getHistory.apply(5);

        assertEquals(expected, result);

        verify(loggerService).log("INFO", "Get history triggered");
        verify(loggerService).cleanupOldLogs(50);
        verify(loggerService).getHistory(5);
    }

    @Test
    void testGetHistoryWithNullInput() throws Exception {
        String expected = "[{\"message\":\"Default logs\"}]";

        when(loggerService.getHistory(10)).thenReturn(expected);

        String result = getHistory.apply(null);

        assertEquals(expected, result);
        verify(loggerService).getHistory(10);
        verify(loggerService).cleanupOldLogs(50);
    }

    @Test
    void testGetHistoryWithZeroInput() throws Exception {
        when(loggerService.getHistory(0)).thenReturn("[]");

        String result = getHistory.apply(0);

        assertEquals("[]", result);
        verify(loggerService).getHistory(0);
    }

    @Test
    void testGetHistoryWhenJsonProcessingFails() throws Exception {
        JsonProcessingException exception =
                new JsonProcessingException("Invalid JSON",
                        new IllegalArgumentException("Parsing failed")) {};

        when(loggerService.getHistory(5)).thenThrow(exception);

        String result = getHistory.apply(5);

        assertEquals("[]", result);

        verify(loggerService).log("INFO", "Get history triggered");
        verify(loggerService).log("INFO", "Parsing failed");
        verify(loggerService).cleanupOldLogs(50);
    }

    @Test
    void testGetHistoryCallsCleanupBeforeReading() throws Exception {
        when(loggerService.getHistory(3)).thenReturn("[]");

        getHistory.apply(3);

        var order = inOrder(loggerService);

        order.verify(loggerService).log("INFO", "Get history triggered");
        order.verify(loggerService).cleanupOldLogs(50);
        order.verify(loggerService).getHistory(3);
    }
}
