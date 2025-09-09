package com.demo.utility_classes;

import java.util.Optional;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

public class ResponseEntityUtil {

	private ResponseEntityUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static HttpResponseMessage buildResponse(HttpRequestMessage<Optional<String>> request, String result) {

		return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json")
				.header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type, Authorization").body(result).build();
	}
	
}
