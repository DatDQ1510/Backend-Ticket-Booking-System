package com.example.demo.dto.seat;

import com.example.demo.entity.enums.SeatStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CreateSeatDTO {

    private Long eventId;

    private int numRows;

    private int seatsPerRow;

    private Long basePrice;

    private SeatStatus status;

    private String seatType;
}
