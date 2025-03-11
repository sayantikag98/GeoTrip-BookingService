package com.geotrip.bookingservice.controllers;

import com.geotrip.bookingservice.clients.AuthServiceClient;
import com.geotrip.bookingservice.dtos.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final AuthServiceClient authServiceClient;

    public TestController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping("/validate")
    public ResponseEntity<String> testAuthService(@RequestHeader("Authorization") String token) {
        System.out.println("Making request to Auth Service with token: " + token);
        ResponseEntity<UserDto> response = authServiceClient.validateToken(token);
        return ResponseEntity.ok("Auth Service Response: " + response.getStatusCode());
    }
}

