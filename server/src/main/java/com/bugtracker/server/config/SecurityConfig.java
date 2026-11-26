package com.bugtracker.server.config;

import com.bugtracker.server.security.JsonAuthenticationEntryPoint;
import com.bugtracker.server.security.JwtAuthenticationFilter;
import com.bugtracker.server.service.i.IAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, IAuthService authService) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(new JsonAuthenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(authService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
