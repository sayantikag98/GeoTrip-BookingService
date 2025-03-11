package com.geotrip.bookingservice.clients;

import com.geotrip.bookingservice.dtos.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/auth")
public interface AuthServiceClient {

//    @PostExchange("/validate")
//    ResponseEntity<UserDto> validateToken(@RequestHeader("Authorization") String token);


    @PostExchange("/validate")
    default ResponseEntity<UserDto> validateToken(@RequestHeader("Authorization") String token) {
        System.out.println("Calling Auth Service with Token: " + token);
        return callValidateToken(token);
    }

    @PostExchange("/validate")
    ResponseEntity<UserDto> callValidateToken(@RequestHeader("Authorization") String token);
}
