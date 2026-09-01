package com.example.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtValidationUtils jwtValidationUtils;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.contains("/api/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            jwtValidationUtils.validateToken(token);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return onError(exchange, ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange);

    }


    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        System.out.println("`error is: " + message);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
