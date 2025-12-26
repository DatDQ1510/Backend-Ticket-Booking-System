package com.example.demo.dto.ticket;

import lombok.Getter;

import java.util.List;

@Getter
public class CreatTicketDTO {
    private Long EventId;
    private List<Long> SeatIds;
    private Long OrderId;
}
