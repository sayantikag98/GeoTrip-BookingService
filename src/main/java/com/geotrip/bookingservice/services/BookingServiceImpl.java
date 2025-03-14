package com.geotrip.bookingservice.services;


import com.geotrip.bookingservice.clients.LocationServiceClient;
import com.geotrip.bookingservice.dtos.*;
import com.geotrip.bookingservice.headers.SecurityContextHeader;
import com.geotrip.bookingservice.repositories.BookingRepository;
import com.geotrip.bookingservice.repositories.DriverRepository;
import com.geotrip.bookingservice.repositories.ExactLocationRepository;
import com.geotrip.bookingservice.repositories.PassengerRepository;
import com.geotrip.entityservice.models.*;
import com.geotrip.exceptionhandler.AppException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final LocationServiceClient locationServiceClient;
    private final SecurityContextHeader securityContextHeader;
    private final Logger logger;
    private final ExactLocationRepository exactLocationRepository;

    //TODO: scheduled booking flow

    @Transactional
    public BookingDto createBooking(CreateBookingRequestDto createBookingRequestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("Not Authenticated", HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();
        System.out.println("Email: "+email);

        Passenger passenger = passengerRepository.findByEmail(email).orElseThrow(() -> new AppException("Passenger not found", HttpStatus.NOT_FOUND));

        Boolean activeBookings = bookingRepository.existsBookingByPassengerEmailAndBookingStatusIn(email, List.of(
                BookingStatus.REQUESTED,
                BookingStatus.SEARCHING_DRIVER,
                BookingStatus.DRIVER_ASSIGNED,
                BookingStatus.DRIVER_ARRIVING,
                BookingStatus.IN_PROGRESS
        ));

        if(activeBookings) throw new AppException("You already have an active ride. Complete or cancel your current ride before booking a new one.", HttpStatus.BAD_REQUEST);

        Booking booking = Booking.builder()
                .passenger(passenger)
                .pickupLocation(
                        findOrCreateExactLocation(createBookingRequestDto.getPickupLocation())
                )
                .dropoffLocation(
                        findOrCreateExactLocation(createBookingRequestDto.getDropOffLocation())
                )
                .bookingStatus(BookingStatus.REQUESTED)
                .build();

        bookingRepository.saveAndFlush(booking);

        assignBookingToDriver(booking.getId());

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


    @Async
    @Transactional
    public void assignBookingToDriver(UUID bookingId) {
        try{
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

                HttpHeaders headers = securityContextHeader.createAuthenticationHeader();
                ResponseEntity<List<DriverLocationDto>> nearbyDriverListResponse = locationServiceClient.getNearbyDrivers(findNearbyDriverRequestDto, headers);
                List<DriverLocationDto> nearbyDriverList = nearbyDriverListResponse.getBody();

                if(nearbyDriverList != null){
                    for(DriverLocationDto driverLocationDto : nearbyDriverList){

                        Boolean isDriverBusy = bookingRepository.existsBookingByDriverIdAndBookingStatusIn(
                                driverLocationDto.getDriverId(),
                                List.of(
                                        BookingStatus.DRIVER_ASSIGNED,
                                        BookingStatus.DRIVER_ARRIVING,
                                        BookingStatus.IN_PROGRESS
                                )
                        );

                        if(isDriverBusy) {
                            continue;
                        }

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
                //TODO: notify passenger about no driver found
                logger.info("No available driver found for the ride");
                throw new AppException("No available driver found for the ride", HttpStatus.NOT_FOUND);
            }

            booking.setDriver(assignedDriver);
            booking.setBookingStatus(BookingStatus.DRIVER_ASSIGNED);
            bookingRepository.save(booking);

            //TODO: notify driver and passenger about this booking
        }
        catch(Exception exception){
            System.out.println(exception.getMessage());
        }

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

    private ExactLocation findOrCreateExactLocation(ExactLocationDto exactLocationDto) {
        Double longitude = exactLocationDto.getLongitude();
        Double latitude = exactLocationDto.getLatitude();

        Optional<ExactLocation> exactLocation = exactLocationRepository.findExactLocationByLongitudeAndLatitude(longitude, latitude);
        if(exactLocation.isPresent()) {
            return exactLocation.get();
        }
        return exactLocationRepository.save(
                    ExactLocation.builder()
                            .longitude(exactLocationDto.getLongitude())
                            .latitude(exactLocationDto.getLatitude())
                            .build()
            );

    }

}
