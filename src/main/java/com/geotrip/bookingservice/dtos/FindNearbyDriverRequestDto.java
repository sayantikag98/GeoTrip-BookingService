package com.geotrip.bookingservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;



@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindNearbyDriverRequestDto {

    @NotNull(message = "Exact Location must not be null")
    private ExactLocationDto exactLocationDto;

    @NotNull(message = "Radius must not be null")
    @Min(value = 0, message = "Radius must be greater than or equal zero")
    private Double radius;

    @NotNull(message = "Limit must not be null")
    @Min(value = 1, message = "Limit must be greater than zero")
    private Long limit;

}
