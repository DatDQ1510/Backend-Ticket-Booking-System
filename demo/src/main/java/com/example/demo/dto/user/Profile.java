package com.example.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private String username;
    private String phoneNumber;
    private String address;
    private String fullName;
    private String birthOfDate;
    private String email;
}