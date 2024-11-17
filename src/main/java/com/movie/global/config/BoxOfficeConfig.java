package com.movie.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class BoxOfficeConfig {

    @Value("${movie.kofic.api.key}")
    private String apiKey;

    @Value("${movie.kofic.api.url}")
    private String apiUrl;

    @Bean
    public RestTemplate boxOfficeRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiUrl);
        restTemplate.setUriTemplateHandler(factory);

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Accept", "application/json");
            request.getHeaders().add("Authorization", apiKey);
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    public String getApiKey() {
        return apiKey;
    }
}
