package com.example.keycloak.configuration;

import com.example.keycloak.component.JwtConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtConverter jwtConverter;

// * Step	
// * 1. CSRF                    Disabled because JWT makes CSRF irrelevant
// * 2. Authorization           All endpoints require authentication
// * 3. Token Decoding	        Spring parses JWT from Authorization: Bearer ...
// * 4. Custom Converter	JwtConverter maps claims into Authentication
// * 5. Session	                No session created — API is fully stateless

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // * Disable CSRF protection
                .authorizeHttpRequests(
                        (authorize) -> authorize.anyRequest().authenticated()
                )
                .oauth2ResourceServer(                                              // * Configures Spring Boot app to act as a Resource Server 
                        (oauth2) -> oauth2.jwt(                                     // * Specifies that JWT tokens are used for authentication.
                                jwt -> jwt.jwtAuthenticationConverter(jwtConverter) // * : Tells Spring how to convert the JWT into a JwtAuthenticationToken, which Spring Security uses for authorization.
                        )
                )
                .sessionManagement( // * tells the Security Config to be stateless and not save any Tokens 
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                );

        return http.build();
    }
}