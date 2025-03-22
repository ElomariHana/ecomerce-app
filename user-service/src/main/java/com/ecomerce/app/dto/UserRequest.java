package com.ecomerce.app.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    private String firstname;
    private String lastname;
    private String email;
}
