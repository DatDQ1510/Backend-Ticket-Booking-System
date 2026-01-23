package com.example.demo.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketQRDataDTO {
    private Long ticketId;
    private Long orderId;
    private Long userId;
    private Long eventId;
    private Long seatId;
    private String email;
    private Long timestamp; // For security verification
}
