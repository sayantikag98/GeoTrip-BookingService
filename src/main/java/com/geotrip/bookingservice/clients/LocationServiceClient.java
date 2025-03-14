package com.geotrip.bookingservice.clients;


import com.geotrip.bookingservice.dtos.DriverLocationDto;
import com.geotrip.bookingservice.dtos.FindNearbyDriverRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/api/v1/location/drivers")
public interface LocationServiceClient {

    @PostExchange("/nearby")
    ResponseEntity<List<DriverLocationDto>> getNearbyDrivers(
            @RequestBody @Valid FindNearbyDriverRequestDto findNearbyDriverRequestDto,
            @RequestHeader HttpHeaders headers
    );
}
