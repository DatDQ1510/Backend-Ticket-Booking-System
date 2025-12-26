package com.example.demo.entity;

import com.example.demo.entity.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "seats",
       indexes = {
           @Index(name = "idx_seat_event", columnList = "eventId"),
           @Index(name = "idx_seat_status", columnList = "status")
       })
@Builder
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    private String seatNumber;

    private String seatRow;

    private String seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    @JsonIgnore
    private EventEntity event;

}
