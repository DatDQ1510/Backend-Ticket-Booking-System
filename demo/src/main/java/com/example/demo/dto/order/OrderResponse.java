package com.example.demo.dto.order;

import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Long amount;
    private String payType;
    private OrderStatus status;
    private String momoTransId;
    private String payUrl; // URL for frontend to redirect to payment page
    private LocalDateTime paidAt;
    private List<TicketResponse> tickets;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketResponse {
        private Long ticketId;
        private Long seatId;
        private String seatNumber;
        private String seatRow;
        private String seatType;
        private Long price;
        private Long eventId;
        private String eventTitle;
        private TicketStatus status;
    }
}
