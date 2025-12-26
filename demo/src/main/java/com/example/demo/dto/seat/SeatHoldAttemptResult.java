package com.example.demo.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldAttemptResult {
    private boolean success;
    private String holdToken;
    private Duration ttl;
    private String message;
}
