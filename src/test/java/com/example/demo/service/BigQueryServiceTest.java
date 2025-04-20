package com.example.demo.service;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BigQueryServiceTest {

    @Test
    void testExecuteQuery() throws InterruptedException {
        // Mock BigQuery and related objects
        BigQuery bigquery = mock(BigQuery.class);
        TableResult tableResult = mock(TableResult.class);

        // Sample query and result
        String sampleQuery = "SELECT * FROM `bigquery-public-data.samples.shakespeare` LIMIT 10";
        List<FieldValueList> sampleRows = new ArrayList<>();
        // Add some mock rows to the sample result if needed

        when(bigquery.query(any(QueryJobConfiguration.class))).thenReturn(tableResult);
        when(tableResult.iterateAll()).thenReturn(sampleRows);

        // Create BigQueryService instance
        BigQueryService bigQueryService = new BigQueryService();
        // Use reflection to set the mocked bigquery instance, as we cannot directly inject it
        try {
            java.lang.reflect.Field field = BigQueryService.class.getDeclaredField("bigquery");
            field.setAccessible(true);
            field.set(bigQueryService, bigquery);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // Handle the exception appropriately, e.g., fail the test
        }


        // Execute the query
        List<String> resultPages = bigQueryService.executeQuery(sampleQuery);

        // Verify the result
        // Adjust the assertion based on your sample data and expected behavior
        assertEquals(1, resultPages.size());
    }
}