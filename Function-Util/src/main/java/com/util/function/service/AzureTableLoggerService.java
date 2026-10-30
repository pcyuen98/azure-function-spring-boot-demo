package com.util.function.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class AzureTableLoggerService {

	private static final String LOGGER_NOT_INITIALIZED_MESSAGE = "AzureTableLoggerService is not initialized.";
	private static final String TIMESTAMP_PROPERTY = "Timestamp";
	private static final String MESSAGE_PROPERTY = "Message";
	private static final String TIME_ZONE_ID = "GMT+8";

	private TableClient tableClient;

	public void initialize(String connectionString, String tableName) {
		this.tableClient = new TableClientBuilder().connectionString(connectionString).tableName(tableName)
				.buildClient();

		try {
			tableClient.createTable();
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("TableAlreadyExists")) {
				// Table already exists, ignore
			} else {
				throw e;
			}
		}
	}

	public String getHistory(int limit) throws JsonProcessingException {
		if (tableClient == null) {
			throw new IllegalStateException(LOGGER_NOT_INITIALIZED_MESSAGE);
		}

		// Fetch all entities
		Iterable<TableEntity> entities = tableClient.listEntities(new ListEntitiesOptions(), null, null);

		// Sort by Timestamp desc and limit to 10
		return AzureTableLoggerService
				.convert2JSON(StreamSupport.stream(entities.spliterator(), false).sorted((e1, e2) -> {
					OffsetDateTime t1 = (OffsetDateTime) e1.getProperty(TIMESTAMP_PROPERTY);
					OffsetDateTime t2 = (OffsetDateTime) e2.getProperty(TIMESTAMP_PROPERTY);
					return t2.compareTo(t1); // descending order
				}).limit(limit).toList());
	}

	public void cleanupOldLogs(int keepLimit) {
		if (tableClient == null) {
			throw new IllegalStateException(LOGGER_NOT_INITIALIZED_MESSAGE);
		}

		// Fetch all entities
		Iterable<TableEntity> entities = tableClient.listEntities(new ListEntitiesOptions(), null, null);

		// Sort by Timestamp desc → skip first N → delete the rest
		List<TableEntity> toDelete = StreamSupport.stream(entities.spliterator(), false).sorted((e1, e2) -> {
			OffsetDateTime t1 = (OffsetDateTime) e1.getProperty(TIMESTAMP_PROPERTY);
			OffsetDateTime t2 = (OffsetDateTime) e2.getProperty(TIMESTAMP_PROPERTY);
			return t2.compareTo(t1); // newest first
		}).skip(keepLimit) // Keep first N rows
				.toList();

		// Delete the rest
		for (TableEntity entity : toDelete) {
			tableClient.deleteEntity(entity.getPartitionKey(), entity.getRowKey());
		}
	}

	public static String convert2JSON(List<TableEntity> history) throws JsonProcessingException {
		// Get the history

		// Convert each TableEntity -> Map with Message & Timestamp(GMT+8)
		List<Map<String, Object>> simplifiedHistory = history
				.stream().map(
						entity -> Map
								.of(MESSAGE_PROPERTY, entity.getProperty(MESSAGE_PROPERTY), TIMESTAMP_PROPERTY,
										((OffsetDateTime) entity.getProperty(TIMESTAMP_PROPERTY))
												.atZoneSameInstant(ZoneId.of(TIME_ZONE_ID)).toLocalDateTime().toString()))
				.toList();

		// Prepare ObjectMapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		// Convert to JSON
		return mapper.writeValueAsString(simplifiedHistory);
	}

	public void log(String level, String message) {
		if (tableClient == null) {
			throw new IllegalStateException(LOGGER_NOT_INITIALIZED_MESSAGE);
		}

		TableEntity entity = new TableEntity("LogsPartition", UUID.randomUUID().toString()).addProperty("Level", level)
				.addProperty(MESSAGE_PROPERTY, message)
				.addProperty(TIMESTAMP_PROPERTY, OffsetDateTime.now(ZoneId.of(TIME_ZONE_ID)));

		tableClient.createEntity(entity);
	}
	
	public void info(String message) {
		if (tableClient == null) {
			throw new IllegalStateException(LOGGER_NOT_INITIALIZED_MESSAGE);
		}

		TableEntity entity = new TableEntity("LogsPartition", UUID.randomUUID().toString()).addProperty("Level", "INFO")
				.addProperty(MESSAGE_PROPERTY, message)
				.addProperty(TIMESTAMP_PROPERTY, OffsetDateTime.now(ZoneId.of(TIME_ZONE_ID)));

		tableClient.createEntity(entity);
	}
}
