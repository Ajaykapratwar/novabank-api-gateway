package com.example.apigateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Component
@Order(-2) //priotertize or make sure this class acts as a default error handler since
// the reactive system behaves differenctly
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        //if the error has been sent already to the client. we don't need to do anything
        if (exchange.getResponse().isCommitted()){
            return Mono.error(ex);
        }

        HttpStatus status = determineStatus(ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", ex.getMessage());
        errorBody.put("path",exchange.getRequest().getPath().value());


        return Mono.fromCallable(()-> objectMapper.writeValueAsBytes(errorBody))
                .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                .flatMap(buffer ->exchange.getResponse().writeWith(Mono.just(buffer)))
                .onErrorResume(e->Mono.error(ex));
    }



    private HttpStatus determineStatus(Throwable ex){
        if (ex instanceof ResponseStatusException rse){
            return (HttpStatus) rse.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;

    }


}