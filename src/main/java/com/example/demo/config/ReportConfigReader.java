package com.example.demo.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Map;

public class ReportConfigReader {

    public static Map<String, Object> readConfig() {
        try {
            Yaml yaml = new Yaml(new Constructor(Map.class));
            InputStream inputStream = ReportConfigReader.class
                    .getClassLoader()
                    .getResourceAsStream("ReportConfig.yaml");
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error reading ReportConfig.yaml", e);
        }
    }
}