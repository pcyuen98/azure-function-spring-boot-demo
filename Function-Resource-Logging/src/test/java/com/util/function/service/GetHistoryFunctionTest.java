package com.util.function.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.util.function.service.AzureTableLoggerService;

import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class GetHistoryFunctionTest {

	@Autowired
	private AzureTableLoggerService loggerService;

	@Test
	void insertDummyRow() throws Exception {
		String dummyString = "unit test insertDummyRow";
		loggerService.info(dummyString);
		
		String history = loggerService.getHistory(1);
		
        assertTrue(history.contains(dummyString), 
                "History should contain the dummy row log entry");
	}
	
	@Test
	void getHistory() throws Exception {

		String history = loggerService.getHistory(10);
		
        assertTrue(history.contains(history), 
                "History should contain the dummy row log entry");
	}
}
