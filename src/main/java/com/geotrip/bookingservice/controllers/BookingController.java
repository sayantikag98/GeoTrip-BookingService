package com.geotrip.bookingservice.controllers;

import com.geotrip.bookingservice.dtos.BookingDto;
import com.geotrip.bookingservice.dtos.CreateBookingRequestDto;
import com.geotrip.bookingservice.dtos.UserDto;
import com.geotrip.bookingservice.clients.AuthServiceClient;
import com.geotrip.bookingservice.services.BookingServiceImpl;
import com.geotrip.entityservice.models.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingServiceImpl bookingService;
    private final AuthServiceClient authServiceClient;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestHeader("Authorization") String token, @RequestBody @Valid CreateBookingRequestDto createBookingRequestDto) {
        UserDto userDto = authServiceClient.validateToken(token).getBody();
        if(userDto == null || userDto.getRole() != Role.PASSENGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BookingDto bookingDto = bookingService.createBooking(userDto, createBookingRequestDto);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID bookingId) {
        BookingDto bookingDto = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BookingDto>> getAllBookingsByPassengerId(@PathVariable UUID passengerId) {
        List<BookingDto> bookingDtoList = bookingService.getAllBookingsByPassengerId(passengerId);
        return ResponseEntity.ok(bookingDtoList);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<BookingDto>> getAllBookingsByDriverId(@PathVariable UUID driverId) {
        List<BookingDto> bookingDtoList = bookingService.getAllBookingsByDriverId(driverId);
        return ResponseEntity.ok(bookingDtoList);
    }

    @GetMapping("/available")
    public ResponseEntity<List<BookingDto>> getAvailableBookings() {
        List<BookingDto> bookingDtoList = bookingService.getAllAvailableBookings();
        return ResponseEntity.ok(bookingDtoList);
    }

    @PatchMapping("/{bookingId}/driver/assign")
    public ResponseEntity<BookingDto> assignBookingToDriver(@PathVariable UUID bookingId) {
        BookingDto bookingDto = bookingService.assignBookingToDriver(bookingId);
        return ResponseEntity.ok(bookingDto);
    }
}
