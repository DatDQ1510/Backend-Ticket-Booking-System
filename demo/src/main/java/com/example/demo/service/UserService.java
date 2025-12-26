package com.example.demo.service;

import com.example.demo.dto.event.EventResponse;
import com.example.demo.dto.event.EventSearchCriteria;
import com.example.demo.dto.user.*;
import com.example.demo.payload.ApiResponse;

import java.util.List;

public interface UserService {
    ApiResponse<UpdateUserDTO> updateUserInfo(UpdateUserDTO updateUserDTO);
    ApiResponse<String> deleteUser(Long id);
    ApiResponse<String> updatePassword(String oldPassword, String newPassword);
    ApiResponse<Profile> getUserProfile(Long userId);
    ApiResponse<List<UsersDTO>>getAllUsers();
    ApiResponse<List<UsersDTO>> searchUser(UserSearchCriteria criteria);
}
