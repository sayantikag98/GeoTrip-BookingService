package com.geotrip.bookingservice.services;


import com.geotrip.bookingservice.clients.LocationServiceClient;
import com.geotrip.bookingservice.dtos.*;
import com.geotrip.bookingservice.repositories.BookingRepository;
import com.geotrip.bookingservice.repositories.DriverRepository;
import com.geotrip.bookingservice.repositories.PassengerRepository;
import com.geotrip.entityservice.models.*;
import com.geotrip.exceptionhandler.AppException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final LocationServiceClient locationServiceClient;

    //TODO: scheduled booking flow

    @Transactional
    public BookingDto createBooking(UserDto userDto, CreateBookingRequestDto createBookingRequestDto) {
        if(userDto.getId() == null) throw new IllegalArgumentException("Passenger id cannot be null");
        Passenger passenger = passengerRepository.findById(userDto.getId()).orElseThrow(() -> new AppException("Passenger not found", HttpStatus.NOT_FOUND));

        Boolean activeBookings = bookingRepository.existsByPassengerIdAndBookingStatusIn(userDto.getId(), List.of(
                BookingStatus.REQUESTED,
                BookingStatus.SEARCHING_DRIVER,
                BookingStatus.DRIVER_ASSIGNED,
                BookingStatus.DRIVER_ARRIVING,
                BookingStatus.IN_PROGRESS
        ));

        if(activeBookings) throw new AppException("You already have an active ride. Complete or cancel your current ride before booking a new one.", HttpStatus.BAD_REQUEST);

        Booking booking = Booking.builder()
                .passenger(passenger)
                .pickupLocation(ExactLocation.builder()
                        .longitude(createBookingRequestDto.getPickupLocation().getLongitude())
                        .latitude(createBookingRequestDto.getPickupLocation().getLatitude())
                        .build()
                )
                .dropoffLocation(ExactLocation.builder()
                        .longitude(createBookingRequestDto.getDropOffLocation().getLongitude())
                        .latitude(createBookingRequestDto.getDropOffLocation().getLatitude())
                        .build()
                )
                .bookingStatus(BookingStatus.REQUESTED)
                .build();

        bookingRepository.save(booking);

        return BookingDto.builder()
                .bookingStatus(booking.getBookingStatus())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .scheduledAt(booking.getScheduledAt())
                .pickupLocation(createBookingRequestDto.getPickupLocation())
                .dropoffLocation(createBookingRequestDto.getDropOffLocation())
                .passenger(new PassengerDto(passenger))
                .driver(booking.getDriver() == null ? null : new DriverDto(booking.getDriver()))
                .build();
    }

    public BookingDto getBookingById(UUID bookingId){
        if(bookingId == null) throw new IllegalArgumentException("Booking id cannot be null");
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));
        return BookingDto.builder()
                .bookingStatus(booking.getBookingStatus())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .scheduledAt(booking.getScheduledAt())
                .pickupLocation(new ExactLocationDto(booking.getPickupLocation()))
                .dropoffLocation(new ExactLocationDto(booking.getDropoffLocation()))
                .passenger(new PassengerDto(booking.getPassenger()))
                .driver(booking.getDriver() == null ? null : new DriverDto(booking.getDriver()))
                .build();
    }

    public List<BookingDto> getAllBookingsByPassengerId(UUID passengerId) {
        if(passengerId == null) throw new IllegalArgumentException("Passenger id cannot be null");
        passengerRepository.findById(passengerId).orElseThrow(() -> new AppException("Passenger not found", HttpStatus.NOT_FOUND));

        List<Booking> bookings = bookingRepository.findByPassengerId(passengerId);

        return convertBookingListToBookingDtoList(bookings);
    }

    public List<BookingDto> getAllBookingsByDriverId(UUID driverId) {
        if(driverId == null) throw new IllegalArgumentException("Driver id cannot be null");

        driverRepository.findById(driverId).orElseThrow(() -> new AppException("Driver not found", HttpStatus.NOT_FOUND));

        List<Booking> bookings = bookingRepository.findByDriverId(driverId);

        return convertBookingListToBookingDtoList(bookings);
    }

    public List<BookingDto> getAllAvailableBookings() {
        List<Booking> bookings = bookingRepository.findByDriverIsNullAndBookingStatus(BookingStatus.REQUESTED);
        return convertBookingListToBookingDtoList(bookings);
    }


    @Transactional
    public BookingDto assignBookingToDriver(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new AppException("Booking not found", HttpStatus.NOT_FOUND));

        if(booking.getDriver() != null) throw new AppException("Driver already assigned to this booking", HttpStatus.BAD_REQUEST);

        if(booking.getBookingStatus() != BookingStatus.REQUESTED && booking.getBookingStatus() != BookingStatus.SEARCHING_DRIVER) {
            throw new AppException("Booking status is not valid to assign driver", HttpStatus.BAD_REQUEST);
        }

        if(booking.getBookingStatus() == BookingStatus.REQUESTED) {
            booking.setBookingStatus(BookingStatus.SEARCHING_DRIVER);
            bookingRepository.save(booking);
        }

        double radius = 2.0;
        final double maxRadius = 10.0;
        final long limit = 5L;

        Driver assignedDriver = null;

        while(radius <= maxRadius) {

            FindNearbyDriverRequestDto findNearbyDriverRequestDto = FindNearbyDriverRequestDto.builder()
                                                                    .exactLocationDto(new ExactLocationDto(booking.getPickupLocation()))
                                                                    .radius(radius)
                                                                    .limit(limit)
                                                                    .build();

            ResponseEntity<List<DriverLocationDto>> nearbyDriverListResponse = locationServiceClient.getNearbyDrivers(findNearbyDriverRequestDto);
            List<DriverLocationDto> nearbyDriverList = nearbyDriverListResponse.getBody();

            if(nearbyDriverList != null){
                for(DriverLocationDto driverLocationDto : nearbyDriverList){
                    Driver driver = driverRepository.findById(driverLocationDto.getDriverId()).orElse(null);
                    if(
                            driver != null &&
                            driver.isActive() &&
                            driver.isEnabled() &&
                            driver.getIsLicenseNumberVerified() &&
                            driver.getApprovalStatus() == DriverApprovalStatus.APPROVED
                    ){
                        assignedDriver = driver;
                        break;
                    }
                }
            }

            if(assignedDriver != null){
                break;
            }

            radius += 2.0;
        }

        if(assignedDriver == null){
            throw new AppException("No available driver found for the ride", HttpStatus.NOT_FOUND);
        }

        booking.setDriver(assignedDriver);
        booking.setBookingStatus(BookingStatus.DRIVER_ASSIGNED);
        bookingRepository.save(booking);

        return BookingDto.builder()
                .bookingStatus(booking.getBookingStatus())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .scheduledAt(booking.getScheduledAt())
                .pickupLocation(new ExactLocationDto(booking.getPickupLocation()))
                .dropoffLocation(new ExactLocationDto(booking.getDropoffLocation()))
                .passenger(new PassengerDto(booking.getPassenger()))
                .driver(new DriverDto(booking.getDriver()))
                .build();
    }

    private List<BookingDto> convertBookingListToBookingDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> BookingDto.builder()
                        .bookingStatus(booking.getBookingStatus())
                        .startTime(booking.getStartTime())
                        .endTime(booking.getEndTime())
                        .scheduledAt(booking.getScheduledAt())
                        .pickupLocation(new ExactLocationDto(booking.getPickupLocation()))
                        .dropoffLocation(new ExactLocationDto(booking.getDropoffLocation()))
                        .passenger(new PassengerDto(booking.getPassenger()))
                        .driver(booking.getDriver() == null ? null : new DriverDto(booking.getDriver()))
                        .build()
                )
                .collect(Collectors.toList());
    }

}
