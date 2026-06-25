package com.FaceRecogSystem.RecogSystem.config;

import com.FaceRecogSystem.RecogSystem.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Fully public ──────────────────────────────────────────
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",        // self-registration
                    "/api/auth/validate-face",   // face detection pre-check
                    "/api/departments",          // needed on register form
                    "/ws/**"
                ).permitAll()

                // ── Admin only ────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/fees/create", "/api/fees/*/pay").hasRole("ADMIN")

                // ── Teacher + Admin ───────────────────────────────────────
                .requestMatchers("/api/classes/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/leaves/pending",
                                 "/api/leaves/*/review").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/attendance/today",
                                 "/api/attendance/date/**",
                                 "/api/attendance/range").hasAnyRole("ADMIN", "TEACHER")

                // ── Student self-service ──────────────────────────────────
                .requestMatchers("/api/students/me/**").hasAnyRole("STUDENT","ADMIN","TEACHER")
                .requestMatchers("/api/leaves/apply").hasAnyRole("STUDENT","ADMIN")

                // ── Everything else requires auth ─────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
