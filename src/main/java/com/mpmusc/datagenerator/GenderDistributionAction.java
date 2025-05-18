package com.mpmusc.datagenerator;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class GenderDistributionAction {

    public static JsonObject main(JsonObject input) {
        JsonObject response = new JsonObject();

        try {
            String filename = input.has("filename") ? input.get("filename").getAsString() : "fake_employees_100k.csv";
            Map<String, Map<String, Double>> result = analyzeGenderDistribution(filename);

            JsonObject dist = new JsonObject();
            for (Map.Entry<String, Map<String, Double>> dept : result.entrySet()) {
                JsonObject genders = new JsonObject();
                for (Map.Entry<String, Double> genderEntry : dept.getValue().entrySet()) {
                    genders.addProperty(genderEntry.getKey(), genderEntry.getValue());
                }
                dist.add(dept.getKey(), genders);
            }

            response.add("distribution", dist);
        } catch (Exception e) {
            response.addProperty("error", "Failed to process file: " + e.getMessage());
        }

        return response;
    }

    private static Map<String, Map<String, Double>> analyzeGenderDistribution(String filename) throws Exception {
        Map<String, Map<String, Double>> distribution = new HashMap<>();
        Map<String, Long> departmentTotals = new HashMap<>();

        InputStream inputStream = GenderDistributionAction.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new Exception("File not found in resources: " + filename);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.readLine(); // Skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                if (tokens.length < 5) continue;

                String gender = tokens[2].trim();
                String department = tokens[4].trim();

                simulateProcessingDelay(); // Just simulates delay

                distribution
                        .computeIfAbsent(department, k -> new HashMap<>())
                        .merge(gender, 1.0, Double::sum);

                departmentTotals.merge(department, 1L, Long::sum);
            }
        }

        // Convert counts to percentages
        for (Map.Entry<String, Map<String, Double>> entry : distribution.entrySet()) {
            String department = entry.getKey();
            Map<String, Double> genderCounts = entry.getValue();
            long departmentTotal = departmentTotals.get(department);

            for (Map.Entry<String, Double> genderEntry : genderCounts.entrySet()) {
                double percentage = (genderEntry.getValue() / departmentTotal) * 100;
                BigDecimal rounded = new BigDecimal(percentage).setScale(2, RoundingMode.HALF_UP);
                genderCounts.put(genderEntry.getKey(), rounded.doubleValue());
            }
        }

        return distribution;
    }

    private static void simulateProcessingDelay() {
        for (int i = 0; i < 10000; i++) {
            double temp = Math.sqrt(i) * Math.pow(i, 0.5);
        }
    }
    // mvn clean package
    // to generate the jar file

}