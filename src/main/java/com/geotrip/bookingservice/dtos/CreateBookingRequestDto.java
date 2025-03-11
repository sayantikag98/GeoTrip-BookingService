package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.ExactLocation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDto {

    @NotNull(message = "Please enter a valid pickup location")
    private ExactLocationDto pickupLocation;

    @NotNull(message = "Please enter a valid dropoff location")
    private ExactLocationDto dropOffLocation;

}
