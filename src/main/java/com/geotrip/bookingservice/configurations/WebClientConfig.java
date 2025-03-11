package com.geotrip.bookingservice.configurations;


import com.geotrip.bookingservice.clients.AuthServiceClient;
import com.geotrip.bookingservice.clients.LocationServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {


    @Bean
    public AuthServiceClient authServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();

        return httpServiceProxyFactory.createClient(AuthServiceClient.class);
    }

    @Bean
    public LocationServiceClient locationServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8082")
                .build();

        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();

        return httpServiceProxyFactory.createClient(LocationServiceClient.class);

    }
}
