package com.example.demo.service.impl;

import com.example.demo.context.UserContext;
import com.example.demo.dto.user.*;
import com.example.demo.entity.UserEntity;
import com.example.demo.payload.ApiResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.specification.UserSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.mapper.UserMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<UpdateUserDTO> updateUserInfo(UpdateUserDTO updateUserDTO) {
        Long userId = UserContext.requireCurrentUserId();
        log.info("Updating user info for userId: {}", userId);

        // Lấy user, ném lỗi nếu không tồn tại
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Map các trường từ DTO vào entity
        userEntity.setAddress(updateUserDTO.getAddress());
        userEntity.setPhoneNumber(updateUserDTO.getPhoneNumber());
        userEntity.setFullName(updateUserDTO.getFullName());
        userEntity.setEmail(updateUserDTO.getEmail());
        userEntity.setDateOfBirth(updateUserDTO.getBirthOfDate());
        userEntity.setUsername(updateUserDTO.getUsername());

        log.info("User updated");
        // Lưu entity
        UserEntity updatedUser = userRepository.save(userEntity);

        // Chuyển entity đã update sang DTO để trả về
        UpdateUserDTO responseDTO = userMapper.toDto(updatedUser);

        return ApiResponse.success("User info updated successfully", responseDTO);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteUser(Long id) {
        Long userId = UserContext.requireCurrentUserId();
        log.info("Deleting user with id: {}", userId);
        userRepository.deleteById(userId);
        return ApiResponse.success("User deleted successfully");
    }

    @Override
    public ApiResponse<String> updatePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.requireCurrentUserId();
        log.info("Updating password for userId: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ApiResponse.success("Password updated successfully", null);
    }

    @Override
    public ApiResponse<String> createPassword(String newPassword) {
        Long userId = UserContext.requireCurrentUserId();
        log.info("Updating password for userId: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ApiResponse.success("Password updated successfully", null);
    }

    @Override
    public ApiResponse<?> getUserDashboardData() {

        LocalDate today = LocalDate.now();

        // Tháng này
        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);

        // Tháng trước (cùng kỳ)
        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).atTime(23, 59, 59);


        return ApiResponse.success("Get full data user Dashboard", userRepository.getUserStatsMTD(
                startThisMonth,
                endThisMonth,
                startLastMonth,
                endLastMonth
        ));

    }

    @Override
    public ApiResponse<Profile> getUserProfile(Long userId) {
        log.info("Fetching profile for userId: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = userMapper.toProfile(user);
        return ApiResponse.success("Profile fetched successfully", profile);
    }

    @Override
    public ApiResponse<List<UsersDTO>> getAllUsers() {
        log.info("Fetching all users");
        List<UsersDTO> users = userMapper.toUsersDto(userRepository.findAll());
        return ApiResponse.success("All users fetched successfully", users);
    }

    @Override
    public ApiResponse<List<UsersDTO>> searchUser(UserSearchCriteria criteria) {
        log.info("Searching users with criteria: {}", criteria);
        Specification <UserEntity> spec_User = Specification
                .where(UserSpecification.hasUsername(criteria.getUsername()))
                .and(UserSpecification.hasEmail(criteria.getEmail()))
                .and(UserSpecification.hasRole(criteria.getRole()));

        List<UserEntity> users = userRepository.findAll(spec_User);
        List<UsersDTO> result = userMapper.toUsersDto(users);
        return ApiResponse.success("User search completed successfully", result);
    }

    @Override
    public ApiResponse<Boolean> checkPassword() {
        boolean check = userRepository.existsByHasPassword(false);
        return ApiResponse.success("Password check completed successfully", check);
    }
}
