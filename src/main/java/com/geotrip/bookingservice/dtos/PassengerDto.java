package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.Passenger;
import com.geotrip.entityservice.models.Role;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDto {
    private String name;
    private String email;
    private String phoneNumber;
    private Double averageRating;
    private Role role;
    private Boolean isEmailVerified;
    private Boolean isPhoneNumberVerified;

    public PassengerDto(Passenger passenger) {
        this.name = passenger.getName();
        this.email = passenger.getEmail();
        this.phoneNumber = passenger.getPhoneNumber();
        this.averageRating = passenger.getAverageRating();
        this.role = passenger.getRole();
        this.isEmailVerified = passenger.getIsEmailVerified();
        this.isPhoneNumberVerified = passenger.getIsPhoneNumberVerified();
    }
}
