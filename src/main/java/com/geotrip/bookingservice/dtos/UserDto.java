package com.geotrip.bookingservice.dtos;

import com.geotrip.entityservice.models.Role;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;

    private String email;

    private Role role;

}
