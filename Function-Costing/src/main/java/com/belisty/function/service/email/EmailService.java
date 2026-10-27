
package com.belisty.function.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import com.belisty.function.util.AzureCostManagementProperties;
import com.belisty.function.util.EmailRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final AzureCostManagementProperties azureCostManagementProperties;

    private final OkHttpClient client = new OkHttpClient();

    /**
     * Send email through EngageLab REST API.
     *
     * @param recipients  List of recipient email addresses
     * @param subject     Email subject
     * @param htmlContent HTML email body
     * @return EngageLab API response body
     * @throws IOException if the API request fails
     */
    public String sendEmail(
            List<String> recipients,
            String subject,
            String htmlContent) throws IOException {

        // Step 1: Validate request
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException(
                    "Recipients cannot be empty");
        }

        if (recipients.stream().anyMatch(
                email -> email == null || email.isBlank())) {
            throw new IllegalArgumentException(
                    "Recipient email cannot be blank");
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Email subject cannot be empty");
        }

        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException(
                    "Email HTML content cannot be empty");
        }

        // Step 2: Prepare Basic Authentication
        String apiUser =
                azureCostManagementProperties.getEngageLabApiUser();

        String apiKey =
                azureCostManagementProperties.getEngageLabApiKey();

        String credentials = apiUser + ":" + apiKey;

        String auth = "Basic " +
                Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

        // Step 3: Build email request
        EmailRequest emailRequest = new EmailRequest(
                azureCostManagementProperties.getEngageLabApiFromEmail(),
                recipients,
                new EmailRequest.Body(
                        subject,
                        new EmailRequest.Content(htmlContent)
                )
        );

        // Step 4: Convert request to JSON
        String bodyString = emailRequest.toJson();

        // WARNING: Enable only for local debugging.
        // Email addresses and HTML may contain personal data.
        log.debug("EngageLab Request JSON: {}", bodyString);

        // Step 5: Prepare JSON request body
        MediaType mediaType = MediaType.parse(
                "application/json; charset=utf-8"
        );

        RequestBody body = RequestBody.create(
                bodyString,
                mediaType
        );

        // Step 6: Build HTTP request
        String apiUrl =
                azureCostManagementProperties.getEngageLabApiBaseUrl();

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", auth)
                .addHeader("Accept", "application/json")
                .build();

        log.info("Sending email through EngageLab");
        log.info("EngageLab API URL: {}", apiUrl);
        log.info("Number of recipients: {}", recipients.size());

        // Step 7: Execute HTTP request
        try (Response response = client.newCall(request).execute()) {

            int statusCode = response.code();

            String responseBody = response.body() != null
                    ? response.body().string()
                    : "";

            log.info("EngageLab HTTP Status: {}", statusCode);

            // Step 8: Handle API errors
            if (!response.isSuccessful()) {

                log.error(
                        "EngageLab API failed. HTTP Status: {}",
                        statusCode
                );

                log.error(
                        "EngageLab Error Response: {}",
                        responseBody
                );

                throw new IOException(
                        "EngageLab API Error. HTTP "
                                + statusCode
                                + ", Response: "
                                + responseBody
                );
            }

            // Step 9: Handle successful response
            log.info(
                    "EngageLab email request accepted. Status: {}",
                    statusCode
            );

            log.debug(
                    "EngageLab Success Response: {}",
                    responseBody
            );

            return responseBody;
        }
    }
}
