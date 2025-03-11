package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.ExactLocation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExactLocationDto {

    @NotNull(message = "Longitude must not be null")
    private Double longitude;

    @NotNull(message = "Latitude must not be null")
    private Double latitude;

    public ExactLocationDto(ExactLocation exactLocation) {
        this.longitude = exactLocation.getLongitude();
        this.latitude = exactLocation.getLatitude();
    }
}


