package com.belisty.function.service.email;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import com.belisty.function.util.AzureCostManagementProperties;
import com.belisty.function.util.EmailRequest;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final AzureCostManagementProperties azureCostManagementProperties;
    private final OkHttpClient client = new OkHttpClient();

    public String sendEmail(List<String> recipients, String subject, String htmlContent) throws IOException {
        String auth = "Basic " + Base64.getEncoder().encodeToString((azureCostManagementProperties.getEngageLabApiUser() + ":" + azureCostManagementProperties.getEngageLabApiKey()).getBytes());

        MediaType mediaType = MediaType.parse("application/json");
        
        EmailRequest emailRequest = new EmailRequest(
        		azureCostManagementProperties.getEngageLabApiFromEmail(),
                recipients,
                new EmailRequest.Body(
                		subject,
                        new EmailRequest.Content(htmlContent)
                )
        );
        
        String bodyString = emailRequest.toJson();

        RequestBody body = RequestBody.create(bodyString, mediaType); 

        Request request = new Request.Builder()
                .url(azureCostManagementProperties.getEngageLabApiBaseUrl())
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", auth)
                .build();
	
        try (Response response = client.newCall(request).execute()) {
            return response.message(); 
        }
		
    }
}