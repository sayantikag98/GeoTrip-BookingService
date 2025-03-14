package com.geotrip.bookingservice.repositories;

import com.geotrip.entityservice.models.ExactLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExactLocationRepository extends JpaRepository<ExactLocation, UUID> {
    Optional<ExactLocation> findExactLocationByLongitudeAndLatitude(Double Longitude, Double Latitude);
}
