package com.example.demo.dto.user;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String createdAt;
    private String updatedAt;
}