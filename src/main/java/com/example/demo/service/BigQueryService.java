package com.example.demo.service;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.util.ArrayList;
import java.util.List;

public class BigQueryService {

  public List<String> executeQuery(String querySql) throws InterruptedException {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(querySql).build();

    TableResult result = bigquery.query(queryConfig);
    List<String> pages = new ArrayList<>();

    StringBuilder pageBuilder = new StringBuilder();
    for (FieldValueList row : result.iterateAll()) {
      pageBuilder.append(row.toString()).append("\n");
      if (pageBuilder.length() > 1000000) { // Adjust the page size as needed (e.g., 1MB)
        pages.add(pageBuilder.toString());
        pageBuilder.setLength(0);
      }
    }
    if (pageBuilder.length() > 0) {
      pages.add(pageBuilder.toString());
    }

    return pages;
  }
}