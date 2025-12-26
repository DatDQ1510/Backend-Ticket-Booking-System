package com.example.demo.service.impl;

import com.example.demo.context.UserContext;
import com.example.demo.dto.ticket.CreatTicketDTO;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.*;
import com.example.demo.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public List<TicketEntity> getTicketsByOrderId(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        List<TicketEntity> tickets = ticketRepository.getTicketsByOrderId(orderId, userId);
        if (tickets != null) {
            return tickets;
        }
        return List.of();
    }
}
