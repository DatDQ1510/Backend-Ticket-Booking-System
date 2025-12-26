package com.example.demo.entity;

import com.example.demo.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_event_ticket", columnList = "eventId"),
                @Index(name = "idx_order_ticket", columnList = "orderId")
        }
)
@Builder
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    @JsonIgnore
    private EventEntity event;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    @JsonIgnore
    private OrderEntity order;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seatId", nullable = false)
    @JsonIgnore
    private SeatEntity seat;

    private TicketStatus status;

}
