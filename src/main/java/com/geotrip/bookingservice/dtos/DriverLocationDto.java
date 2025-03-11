package com.geotrip.bookingservice.dtos;

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
