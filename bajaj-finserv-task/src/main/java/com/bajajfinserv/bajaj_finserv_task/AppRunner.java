package com.bajajfinserv.bajaj_finserv_task;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> registrationDetails = new HashMap<>();
        registrationDetails.put("name", "John Doe");
        registrationDetails.put("regNo", "REG12347");
        registrationDetails.put("email", "john@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(registrationDetails, headers);
        System.out.println("Generating webhook...");
        ResponseEntity<Map> response = restTemplate.postForEntity(generateWebhookUrl, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, String> responseBody = response.getBody();
            String webhookUrl = responseBody.get("webhook");
            String accessToken = responseBody.get("accessToken");
            if (webhookUrl != null && accessToken != null) {
                System.out.println("Webhook generated successfully.");
                System.out.println("Access Token: " + accessToken);
                String regNo = registrationDetails.get("regNo");
                int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
                String finalQuery;
                if (lastTwoDigits % 2 == 0) {
                    System.out.println("Registration number is EVEN. Using Query 2.");
                    finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC;";
                } else {
                    System.out.println("Registration number is ODD. Using Query 1.");
                    finalQuery = "SELECT 'This is the placeholder for Question 1';";
                }
                submitFinalQuery(webhookUrl, accessToken, finalQuery);
            } else {
                System.err.println("webhookUrl or accessToken missing in response body!");
                System.err.println("Response Body: " + responseBody);
            }
        } else {
            System.err.println("Failed to generate webhook. Status: " + response.getStatusCode());
            System.err.println("Response Body: " + response.getBody());
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken, String sqlQuery) {
        Map<String, String> submissionBody = new HashMap<>();
        submissionBody.put("finalQuery", sqlQuery);
        HttpHeaders submissionHeaders = new HttpHeaders();
        submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
        submissionHeaders.set("Authorization", accessToken);
        HttpEntity<Map<String, String>> submissionEntity = new HttpEntity<>(submissionBody, submissionHeaders);
        System.out.println("Submitting final query to: " + webhookUrl);
        ResponseEntity<String> submissionResponse = restTemplate.postForEntity(webhookUrl, submissionEntity, String.class);
        if (submissionResponse.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully submitted the query!");
            System.out.println("Submission Response: " + submissionResponse.getBody());
        } else {
            System.err.println("Failed to submit query. Status: " + submissionResponse.getStatusCode());
            System.err.println("Submission Response Body: " + submissionResponse.getBody());
        }
    }
}