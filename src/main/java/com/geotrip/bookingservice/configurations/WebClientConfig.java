package com.geotrip.bookingservice.configurations;

import com.geotrip.bookingservice.clients.LocationServiceClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {


    @LoadBalanced
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public LocationServiceClient locationServiceClient() {
        WebClient webClient = webClientBuilder()
                .baseUrl("http://LOCATION-SERVICE")
                .build();

        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();

        return httpServiceProxyFactory.createClient(LocationServiceClient.class);

    }
}
