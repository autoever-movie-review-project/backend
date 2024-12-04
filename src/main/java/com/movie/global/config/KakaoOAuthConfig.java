package com.movie.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class KakaoOAuthConfig {

    @Value("${kakao.oauth2.client-id}")
    private String clientId;

    @Value("${kakao.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.oauth2.token-uri}")
    private String tokenUri;

    @Value("${kakao.oauth2.user-info-uri}")
    private String userInfoUri;

    @Bean
    public RestTemplate kakaoRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(tokenUri);
        restTemplate.setUriTemplateHandler(factory);

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    public String getClientId() {
        return clientId;
    }


    public String getRedirectUri() {
        return redirectUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }
}
