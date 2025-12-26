package com.example.demo.service;

import com.example.demo.dto.seat.SeatHoldAttemptResult;

import java.time.Duration;
import java.util.List;

/**
 * Seat hold (Redis) service.
 *
 * Purpose:
 * - Hold seats for a short TTL (e.g. 10 minutes) before redirecting to payment.
 * - Provide a holdToken that the frontend can include when creating payment.
 *
 * NOTE: Method bodies are intentionally left for you to implement.
 */
public interface SeatHoldService {

    /**
     * Attempt to hold all requested seats atomically.
     * If any seat is already held, the whole operation should fail.
     */
    SeatHoldAttemptResult holdSeatsOrFail(Long eventId, List<Long> seatIds, Long userId, Duration ttl);

    /**
     * Optional: validate that a given holdToken still owns the hold for all seats.
     * You can skip this if you only rely on Redis Lua + server-side order creation.
     */
    boolean isHoldTokenValid(Long eventId, List<Long> seatIds, String holdToken);

    /**
     * Release holds (e.g. payment failed/cancelled).
     */
    long releaseHold(Long eventId, List<Long> seatIds, String holdToken);

    /**
     * Confirm hold after payment success (optional hook).
     * Typically: verify token, then proceed to update DB seats to SOLD/BOOKED and delete hold keys.
     */
    void confirmHoldAfterPayment(Long orderId, Long eventId, List<Long> seatIds, String holdToken);
}
