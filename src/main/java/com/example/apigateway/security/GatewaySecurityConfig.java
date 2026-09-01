package com.example.apigateway.security;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec.anyExchange().permitAll() // Allow the Global Filter to handle the authentication
                ).build();
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-account-service", r -> r.path("/api/auth/**", "/api/users/**", "/api/accounts/**")
                                .uri("lb://user-account-service") // this depends on the discover Eureka service
//                                .uri("http://localhost:8081") //this allows you to communicate with the intending service by by-passing the eureka discovery service
                )

                .route("transaction-service", r -> r.path("/api/transactions/**")
                                .uri("lb://transaction-service")
//                                .uri("http://localhost:8082")
                )
                .build();

    }
}
