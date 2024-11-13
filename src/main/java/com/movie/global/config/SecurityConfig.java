package com.movie.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.domain.user.dao.LogoutAccessTokenRedisRepository;
import com.movie.domain.user.service.UserRedisService;
import com.movie.global.jwt.JwtTokenProvider;
import com.movie.global.security.filter.CustomAuthenticationFilter;
import com.movie.global.security.handler.CustomAccessDeniedHandler;
import com.movie.global.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRedisService userRedisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()

                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .authorizeRequests()
                // 모든 요청에 대해 인증 없이 접근 가능하도록 설정
                .anyRequest().permitAll()

                .and()
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper))
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper))
                .and()
                .addFilterBefore(
                        new CustomAuthenticationFilter(userRedisService, jwtTokenProvider, objectMapper, logoutAccessTokenRedisRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
