package com.example.demo.service;

import com.example.demo.dto.ticket.CreatTicketDTO;
import com.example.demo.entity.TicketEntity;

import java.util.List;

public interface TicketService {
    List<TicketEntity> getTicketsByOrderId(Long orderId);

}
