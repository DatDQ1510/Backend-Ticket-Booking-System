package com.example.demo.dto.seat;

import com.example.demo.entity.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;
    private String seatNumber;
    private String seatRow;
    private String seatType;
    private SeatStatus status;
    private Long price;
}