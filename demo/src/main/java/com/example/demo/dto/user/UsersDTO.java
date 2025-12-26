package com.example.demo.dto.user;

import lombok.Data;

import java.sql.Date;

@Data
public class UsersDTO {
    private String fullName;
    private String phoneNumber;
    private String address;
    private Date birthOfDate;
    private String email;
    private String username;
}
