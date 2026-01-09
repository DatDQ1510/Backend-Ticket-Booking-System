package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.user.*;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller - Demonstrates UserContext usage
 * 
 * ⭐ THIS SHOWS HOW TO USE UserContext TO GET userId QUICKLY!
 */
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<Profile> getUserProfile() {
        // ⭐ Get userId from context
        Long userId = UserContext.requireCurrentUserId(); // Throws exception if not authenticated

        return userService.getUserProfile(userId);
    }

    @PatchMapping("/update")
    public ApiResponse<UpdateUserDTO> updateUserProfile(UpdateUserDTO updateUserDTO) {

        return userService.updateUserInfo(updateUserDTO);
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> deleteUser() {
        Long userId = UserContext.requireCurrentUserId();
        return userService.deleteUser(userId);
    }

    @PatchMapping("/update-password")
    public ApiResponse<String> updateUserPassword( @RequestBody FormUpdatePassword form) {
        String oldPassword = form.getOldPassword();
        String newPassword = form.getNewPassword();
        return userService.updatePassword(oldPassword , newPassword);
    }

    @PatchMapping("/create-password")
    public ApiResponse<String> createUserPassword( @RequestBody FormCreatePassword form) {
        String newPassword = form.getNewPassword();
        return userService.createPassword(newPassword);
    }
    @GetMapping("/check-password")
    public ApiResponse<Boolean> checkUserPassword() {
        return userService.checkPassword();
    }

    @GetMapping("/all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UsersDTO>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UsersDTO>> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUser(criteria);
    }

    @GetMapping("/dashboard")
    public ApiResponse<UserDashboardDTO> getUserDashboardData() {

        UserDashboardDTO dashboardDTO = new UserDashboardDTO(
                2547,
                342,
                298
        );
        return ApiResponse.success("Get full data user Dashboard", dashboardDTO);
//        return userService.getUserDashboardData();
    }
}
