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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login.html", "/register.html", "/dashboard.html",
                        "/css/**", "/js/**", "/vendor/**").permitAll()
                .requestMatchers("/api/auth/register").permitAll()

                // Department LIST is public (needed by the registration page dropdown).
                // GET only, exact path — department create/edit/delete stays restricted below.
                .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()

                // Reference/master data: everyone logged in can READ
                .requestMatchers(HttpMethod.GET, "/api/departments/**", "/api/suppliers/**",
                        "/api/product-types/**", "/api/expenditure-units/**")
                    .hasAnyRole("TEACHER", "MANAGEMENT", "EXPENDITURE_UNIT_HEAD", "ADMIN")

                // Reference/master data: only MANAGEMENT/ADMIN can WRITE
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