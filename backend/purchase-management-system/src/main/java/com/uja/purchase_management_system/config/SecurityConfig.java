package com.uja.purchase_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/login.html", "/register.html", "/dashboard.html",
                        "/css/**", "/js/**", "/vendor/**").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments/**", "/api/suppliers/**",
                        "/api/product-types/**", "/api/expenditure-units/**")
                    .hasAnyRole("TEACHER", "MANAGEMENT", "EXPENDITURE_UNIT_HEAD", "ADMIN")
                .requestMatchers("/api/departments/**", "/api/suppliers/**",
                        "/api/product-types/**", "/api/expenditure-units/**")
                    .hasAnyRole("MANAGEMENT", "ADMIN")
                .requestMatchers("/api/purchase-orders/**").hasAnyRole("TEACHER", "MANAGEMENT", "EXPENDITURE_UNIT_HEAD", "ADMIN")
                .requestMatchers("/api/attachments/**").hasAnyRole("TEACHER", "MANAGEMENT", "EXPENDITURE_UNIT_HEAD", "ADMIN")
                .requestMatchers("/api/statistics/**").hasAnyRole("MANAGEMENT", "EXPENDITURE_UNIT_HEAD", "ADMIN")
                .requestMatchers("/api/access-logs/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }
}