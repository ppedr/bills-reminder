package com.example.bills.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailjetEmailService implements EmailService {
    private final MailjetClient client;
    
    @Value("${mailjet.from}")
    private String fromEmail;

    public MailjetEmailService(@Value("${mailjet.api-key}") String apiKey,
                               @Value("${mailjet.api-secret}") String apiSecret) {
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecret)
                .build();
        this.client = new MailjetClient(options);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        MailjetRequest request = new MailjetRequest(com.mailjet.client.resource.Emailv31.resource)
                .property(com.mailjet.client.resource.Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(com.mailjet.client.resource.Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", fromEmail))
                                .put(com.mailjet.client.resource.Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", to)))
                                .put(com.mailjet.client.resource.Emailv31.Message.SUBJECT, subject)
                                .put(com.mailjet.client.resource.Emailv31.Message.TEXTPART, body)));
        try {
            client.post(request);
        } catch (MailjetException e) {
            log.error("Failed to send email via Mailjet: {}", e.getMessage());
        }
    }
}
