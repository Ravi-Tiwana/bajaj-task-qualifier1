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
        // Step 4 will go here
        generateWebhookAndSubmitQuery();
    }

    private void generateWebhookAndSubmitQuery() {
        // Step 5: Generate Webhook
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        // Create the request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347"); // Assuming odd for Question 1
        requestBody.put("email", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(generateWebhookUrl, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, String> responseBody = response.getBody();
            String webhookUrl = responseBody.get("webhookUrl");
            String accessToken = responseBody.get("accessToken");

            // Step 6: Submit the SQL Query
            submitFinalQuery(webhookUrl, accessToken);
        } else {
            System.err.println("Failed to generate webhook. Status: " + response.getStatusCode());
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken) {
        // The SQL query from Step 1
        String sqlQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC;";

        // Create the request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("finalQuery", sqlQuery);

        // Set headers, including the JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // The submission URL is the webhook URL received
        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Successfully submitted the query!");
            System.out.println("Response: " + response.getBody());
        } else {
            System.err.println("Failed to submit query. Status: " + response.getStatusCode());
        }
    }
}