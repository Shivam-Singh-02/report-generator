package com.example.demo.controller;

import com.example.demo.config.ReportConfigReader;
import com.example.demo.formatter.CsvFormatter;
import com.example.demo.formatter.JsonFormatter;
import com.example.demo.formatter.ReportFormatter;
import com.example.demo.service.BigQueryService;
import com.example.demo.service.GCSService;
import com.example.demo.transformer.DataTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    @Autowired
    private BigQueryService bigQueryService;

    @Autowired
    private GCSService gcsService;

    @Autowired
    private ReportConfigReader reportConfigReader;

    @PostMapping("/generateReport")
    public ResponseEntity<String> generateReport(@RequestParam String reportName,
                                                 @RequestParam(required = false) String start_date,
                                                 @RequestParam(required = false) String end_date) {
        try {
            Map<String, Object> reportConfig = reportConfigReader.getReportConfig(reportName);
            if (reportConfig == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report configuration not found: " + reportName);
            }

            String query = (String) reportConfig.get("bigquery_query");
            String gcsPath = (String) reportConfig.get("gcs_path");
            String transformationClass = (String) reportConfig.get("transformation_class");
            String reportFormat = (String) reportConfig.getOrDefault("report_format", "CSV");

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(1);

            if (start_date != null && end_date != null) {
                try {
                    startDate = LocalDate.parse(start_date, DateTimeFormatter.ISO_LOCAL_DATE);
                    endDate = LocalDate.parse(end_date, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format. Using default dates.");
                }
            }

            query = query.replace("{{start_day}}", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            query = query.replace("{{end_day}}", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            List<String> queryResults = bigQueryService.executeQuery(query);

            if (transformationClass != null && !transformationClass.isEmpty()) {
                try {
                    Class<?> transformerClass = Class.forName(transformationClass);
                    Constructor<?> constructor = transformerClass.getConstructor();
                    DataTransformer transformer = (DataTransformer) constructor.newInstance();
                    StringBuilder transformedData = new StringBuilder();
                    for (String page : queryResults) {
                        transformedData.append(transformer.transform(page));
                    }
                    queryResults = List.of(transformedData.toString());
                } catch (Exception e) {
                    System.err.println("Error during transformation: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during data transformation.");
                }
            }

            ReportFormatter formatter = null;
            if (reportFormat.equalsIgnoreCase("CSV")) {
                formatter = new CsvFormatter();
            } else if (reportFormat.equalsIgnoreCase("JSON")) {
                formatter = new JsonFormatter();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported report format: " + reportFormat);
            }

            String formattedData = formatter.format(queryResults);

            String filename = reportName + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    (reportFormat.equalsIgnoreCase("CSV") ? ".csv" : ".json");
            gcsService.storeData(formattedData, gcsPath + filename, reportFormat);

            return ResponseEntity.ok("Report generated successfully and stored in GCS.");

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating report: " + e.getMessage());
        }
    }
}