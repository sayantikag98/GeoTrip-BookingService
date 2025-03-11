package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.BookingStatus;
import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private BookingStatus bookingStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime scheduledAt;

    private ExactLocationDto pickupLocation;

    private ExactLocationDto dropoffLocation;

    private PassengerDto passenger;

    private DriverDto driver;

}
