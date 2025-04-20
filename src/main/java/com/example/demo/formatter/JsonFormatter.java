package com.example.demo.formatter;

import com.example.demo.ReportFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvFormatter implements ReportFormatter {

  @Override
  public String format(List<String> data) {
    if (data == null || data.isEmpty()) {
      return "";
    }

    StringBuilder csvContent = new StringBuilder();
    for (String page : data) {
      csvContent.append(page).append("\n");
    }

    return csvContent.toString();
  }
}