package com.quest.etna.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter,
                          AuthenticationProvider authenticationProvider,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.authenticationProvider = authenticationProvider;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .cors()
        .and()
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            // Autoriser les accès sans authentification pour Swagger
        	.requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**").permitAll()
            .requestMatchers("/authenticate", "/register", "/refresh-token").permitAll()
            .requestMatchers("/uploads/profile_pictures/**").permitAll()
            .requestMatchers("/menu-item").permitAll()
            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/order/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/address/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/address").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
            // Autoriser l'accès à l'endpoint du menu sans authentification
        )
        .authenticationProvider(authenticationProvider)
        .exceptionHandling()
            .authenticationEntryPoint(customAuthenticationEntryPoint)
        .and()
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // Configuration globale CORS
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:4200");  // Autoriser l'origine Angular
        corsConfig.addAllowedHeader("*");  // Autoriser tous les en-têtes
        corsConfig.addAllowedMethod("*");  // Autoriser toutes les méthodes HTTP (GET, POST, etc.)
        corsConfig.setAllowCredentials(true);  // Autoriser les cookies (si nécessaire)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);  // Appliquer à toutes les routes

        return new CorsFilter(source);
    }
}
