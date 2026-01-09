package com.example.demo.dto.user;

import lombok.Data;

@Data
public class UserDashboardDTO {
    private long total_users;
    private long users_this_month;
    private long users_last_month;

    public UserDashboardDTO(long total_users, long users_this_month, long users_last_month) {
        this.total_users = total_users;
        this.users_this_month = users_this_month;
        this.users_last_month = users_last_month;
    }
}