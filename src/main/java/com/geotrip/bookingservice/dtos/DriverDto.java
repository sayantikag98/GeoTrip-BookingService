package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.Driver;
import com.geotrip.entityservice.models.DriverApprovalStatus;
import com.geotrip.entityservice.models.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private Double averageRating;
    private Boolean isActive;
    private DriverApprovalStatus approvalStatus;
    private Role role;
    private Boolean isEmailVerified;
    private Boolean isPhoneNumberVerified;
    private Boolean isLicenseNumberVerified;


    public DriverDto(Driver driver) {
        this.name = driver.getName();
        this.email = driver.getEmail();
        this.phoneNumber = driver.getPhoneNumber();
        this.licenseNumber = driver.getLicenseNumber();
        this.averageRating = driver.getAverageRating();
        this.isActive = driver.isActive();
        this.approvalStatus = driver.getApprovalStatus();
        this.role = driver.getRole();
        this.isEmailVerified = driver.getIsEmailVerified();
        this.isPhoneNumberVerified = driver.getIsPhoneNumberVerified();
        this.isLicenseNumberVerified = driver.getIsLicenseNumberVerified();
    }
}
