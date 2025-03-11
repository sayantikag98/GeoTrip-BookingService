package com.geotrip.bookingservice.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationDto {

    private UUID driverId;

    private ExactLocationDto exactLocationDto;

    private Double distance;
}
