package com.geotrip.bookingservice.headers;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHeader {

    private static final String HEADER_EMAIL = "X-User-Email";
    private static final String HEADER_ROLE = "X-User-Role";


    public HttpHeaders createAuthenticationHeader() {
        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            String role = authentication.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse(null);

            if(email != null && role != null) {
                headers.add(HEADER_EMAIL, email);
                headers.add(HEADER_ROLE, role);
            }
        }
        return headers;
    }


}
