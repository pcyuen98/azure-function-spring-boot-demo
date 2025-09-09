package com.belisty.function.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.belisty.function.model.GetCache;

public interface CacheRepo extends MongoRepository<GetCache, String> {

}