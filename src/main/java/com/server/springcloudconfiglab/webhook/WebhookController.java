package com.server.springcloudconfiglab.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WebhookController {

    @PostMapping("/webhook")
    public void handleWebhook(){
        System.out.println("Git push detected!");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        try {
            restTemplate.postForObject(
            "http://localhost:8888/actuator/busrefresh",
            entity,
            String.class
            );
            System.out.println("Bus Refresh 요청 완료!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
