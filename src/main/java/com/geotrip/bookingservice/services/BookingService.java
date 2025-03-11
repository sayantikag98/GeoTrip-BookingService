package com.geotrip.bookingservice.services;

import com.geotrip.bookingservice.dtos.*;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingDto createBooking(UserDto userDto, CreateBookingRequestDto createBookingRequestDto);

    BookingDto getBookingById(UUID bookingId);

    List<BookingDto> getAllBookingsByPassengerId(UUID passengerId);

    List<BookingDto> getAllBookingsByDriverId(UUID driverId);

    List<BookingDto> getAllAvailableBookings();

    BookingDto assignBookingToDriver(UUID bookingId);
}
