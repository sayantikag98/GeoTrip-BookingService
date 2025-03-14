package com.geotrip.bookingservice.controllers;

import com.geotrip.bookingservice.dtos.BookingDto;
import com.geotrip.bookingservice.dtos.CreateBookingRequestDto;
import com.geotrip.bookingservice.services.BookingServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingServiceImpl bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PASSENGER')")
    public ResponseEntity<BookingDto> createBooking(@RequestBody @Valid CreateBookingRequestDto createBookingRequestDto) {
        BookingDto bookingDto = bookingService.createBooking(createBookingRequestDto);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID bookingId) {
        BookingDto bookingDto = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAuthority('ROLE_PASSENGER')")
    public ResponseEntity<List<BookingDto>> getAllBookingsByPassengerId(@PathVariable UUID passengerId) {
        List<BookingDto> bookingDtoList = bookingService.getAllBookingsByPassengerId(passengerId);
        return ResponseEntity.ok(bookingDtoList);
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<List<BookingDto>> getAllBookingsByDriverId(@PathVariable UUID driverId) {
        List<BookingDto> bookingDtoList = bookingService.getAllBookingsByDriverId(driverId);
        return ResponseEntity.ok(bookingDtoList);
    }

//    @GetMapping("/available")
//    public ResponseEntity<List<BookingDto>> getAvailableBookings() {
//        List<BookingDto> bookingDtoList = bookingService.getAllAvailableBookings();
//        return ResponseEntity.ok(bookingDtoList);
//    }
//
//    @PatchMapping("/{bookingId}/driver/assign")
//    public ResponseEntity<BookingDto> assignBookingToDriver(@PathVariable UUID bookingId) {
//        BookingDto bookingDto = bookingService.assignBookingToDriver(bookingId);
//        return ResponseEntity.ok(bookingDto);
//    }
}
