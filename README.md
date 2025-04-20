# BigQuery Report Generator

This project is a Java Spring Boot application that generates reports by querying data from Google BigQuery and storing the results in Google Cloud Storage. It uses a configurable approach, allowing you to define multiple reports with different queries, schedules, and storage locations.

## Technologies Used

*   Java
*   Spring Boot
*   Gradle
*   Google Cloud BigQuery
*   Google Cloud Storage
*   Docker
*   JUnit 5 (for testing)

## Prerequisites

*   Java JDK 17 or higher
*   Gradle 7 or higher
*   Docker
*   Google Cloud SDK (gcloud)
*   A Google Cloud project with BigQuery and Cloud Storage APIs enabled

## Build Instructions

1.  Clone the repository:
```
bash
    git clone <repository_url>
    cd <project_directory>
    
```
2.  Build the project using Gradle:
```
bash
    gradle build
    
```
This will compile the code, run the tests, and create a JAR file in the `build/libs` directory.

## Configuration

1.  **Report Configuration:**

    The reports are configured in the `src/main/resources/ReportConfig.yaml` file. This file defines the BigQuery query, schedule, Google Cloud Storage path, optional data transformation class, and report format for each report.

    Example `ReportConfig.yaml`:
```
yaml
    report1:
      bigquery_query: "SELECT * FROM `your-project.your_dataset.your_table` WHERE date >= '{{start_date}}' AND date <= '{{end_date}}'"
      schedule: "0 0 * * *"  # Daily at midnight
      gcs_path: "gs://your-bucket/report1/"
      transformation_class: "com.example.demo.transformer.MyDataTransformer" # Optional
      report_format: "CSV" # or "JSON", defaults to "CSV"

    report2:
      bigquery_query: "SELECT column1, column2 FROM `another_project.another_dataset.another_table` WHERE event_time BETWEEN TIMESTAMP('{{start_date}}') AND TIMESTAMP('{{end_date}}')"
      schedule: "0 12 * * MON"  # Weekly, every Monday at noon
      gcs_path: "gs://your-bucket/report2/"
      report_format: "JSON"
    
```
*   `bigquery_query`: The SQL query to execute in BigQuery. Use `{{start_date}}` and `{{end_date}}` as placeholders for dynamic date filtering.
    *   `schedule`: A cron expression defining the report generation schedule.
    *   `gcs_path`: The Google Cloud Storage path where the report will be stored.
    *   `transformation_class`: (Optional) The fully qualified name of a Java class that implements the `DataTransformer` interface for data transformation.
    *   `report_format`: (Optional) The desired report output format, either "CSV" or "JSON". Defaults to "CSV".

2.  **Google Cloud Credentials:**

    The application uses the default Google Cloud credentials configured in your environment. Ensure that you have the necessary permissions to access BigQuery and Google Cloud Storage in your project. You can set up Application Default Credentials (ADC) by running:
```
bash
    gcloud auth application-default login
    
```
## Running the Application

### Locally (for testing):

You can run the Spring Boot application locally for testing purposes:
```
bash
./gradlew bootRun
```
This will start the application and expose the HTTP endpoint for triggering report generation. You can then send a POST request to `http://localhost:8080/generateReport` with the report name as a parameter (e.g., `report1`).  You can also include `start_date` and `end_date` parameters in the format `YYYY-MM-DD` to override the default date range (previous day to current day).

### Deploying to Google Cloud Run:

1.  **Build the Docker image:**
```
bash
    docker build -t gcr.io/<your-project-id>/bigquery-report-generator:latest .
    
```
Replace `<your-project-id>` with your Google Cloud project ID.

2.  **Push the image to Google Container Registry:**
```
bash
    docker push gcr.io/<your-project-id>/bigquery-report-generator:latest
    
```
3.  **Deploy to Cloud Run:**
```
bash
    gcloud run deploy bigquery-report-generator --image gcr.io/<your-project-id>/bigquery-report-generator:latest --region <your-region> --allow-unauthenticated
    
```
Replace `<your-region>` with the desired Google Cloud region. You will be prompted to choose a service name and whether to allow unauthenticated access (required for Cloud Scheduler to trigger the reports).

    Note the service URL provided after successful deployment.

## Running Tests

To run the unit tests for the project, use the following Gradle command:
```
bash
gradle test
```
## Cloud Scheduler Setup

To automate report generation, you need to set up Cloud Scheduler jobs to trigger the Cloud Run service.

1.  **Create a Cloud Scheduler Job:**

    For each report defined in `ReportConfig.yaml`, create a Cloud Scheduler job with the following settings:

    *   **Name:** A descriptive name for the job (e.g., `generate-report1`).
    *   **Frequency:** The cron expression from the `schedule` key in `ReportConfig.yaml`.
    *   **Target type:** HTTP
    *   **URL:** The Cloud Run service URL (obtained during deployment) + `/generateReport` (e.g., `https://bigquery-report-generator-xyz.run.app/generateReport`).
    *   **HTTP method:** POST
    *   **Body:**  The report name as a JSON string. For example, for `report1`:
```
json
        {"reportName": "report1"}
        
```
You can optionally include "start_date" and "end_date" parameters in YYYY-MM-DD format. For example:
```
json
        {"reportName": "report1", "start_date": "2024-01-01", "end_date": "2024-01-31"}
        
```
*   **Headers:** Add a header:
        *   **Name:** `Content-Type`
        *   **Value:** `application/json`

2.  **Repeat for Each Report:**

    Create a separate Cloud Scheduler job for each report defined in `ReportConfig.yaml`, adjusting the job name, frequency, and body accordingly.

Once the Cloud Scheduler jobs are created, they will automatically trigger the report generation process according to their configured schedules.