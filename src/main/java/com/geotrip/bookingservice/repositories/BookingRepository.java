package com.geotrip.bookingservice.repositories;

import com.geotrip.entityservice.models.Booking;
import com.geotrip.entityservice.models.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByPassengerId(UUID passengerId);
    List<Booking> findByDriverId(UUID driverId);
    List<Booking> findByDriverIsNullAndBookingStatus(BookingStatus bookingStatus);
    Boolean existsByPassengerIdAndBookingStatusIn(UUID passengerId, List<BookingStatus> bookingStatuses);
}
