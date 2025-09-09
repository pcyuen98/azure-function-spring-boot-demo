package com.belisty.function.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "cache")
public class GetCache {

    @Id
    private String id;
    
	private String data;
}
