package com.belisty.function.service;

import java.util.List;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.belisty.function.model.GetCache;
import com.belisty.function.repo.CacheRepo;

import lombok.AllArgsConstructor;

/**
 * Azure Function using Spring Cloud Function style. This will be triggered via
 * FunctionInvoker.
 */
@Component
@AllArgsConstructor
public class GetCacheFunction {

	private final AzureTableLoggerService loggerService;
	private final CacheRepo cacheRepo;

	@Bean(name = "putCache")
	public Function<GetCache, String> putCache() {
		return input -> {

			List<GetCache> allCaches = cacheRepo.findAll();
			if (!allCaches.isEmpty()) {
				// First record found → update it
				GetCache existingCache = allCaches.get(0);
				existingCache.setData(input.getData()); // update data

				// Delete the rest of the records
				if (allCaches.size() > 1) {
					cacheRepo.deleteAll(allCaches.subList(1, allCaches.size()));
				}

				return cacheRepo.save(existingCache).getData(); // persist update
			} else {
				// No records found → create a new one
				return cacheRepo.save(input).getData();
			}
		};
	}

	@Bean(name = "getCache")
	public Function<GetCache, String> getCache() {
		return input -> {
			GetCache cache = new GetCache();
			try {
				loggerService.log("info", "getCache2");
				List<GetCache> allCaches = cacheRepo.findAll();
				loggerService.log("info", "getCache3-->" + allCaches.size());
				if (!allCaches.isEmpty()) {
					return allCaches.get(0).getData(); // first record found
				} else {
					cache.setData("[]");

				}
			} catch (Exception e) {
				loggerService.log("info", "exception-->" + e.toString());
			}
			return cache.getData();
		};
	}
}
