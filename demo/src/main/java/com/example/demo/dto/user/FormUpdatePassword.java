package com.example.demo.dto.user;

import lombok.Data;

@Data
public class FormUpdatePassword {

    private String oldPassword;
    private String newPassword;

}
