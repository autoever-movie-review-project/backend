package com.movie.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class TmdbConfig {

    @Value("${movie.tmdb.api.key}")
    private String apiKey;

    @Value("${movie.tmdb.api.url}")
    private String apiUrl;

    @Bean
    public RestTemplate tmdbRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiUrl);
        restTemplate.setUriTemplateHandler(factory);

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + apiKey);
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
