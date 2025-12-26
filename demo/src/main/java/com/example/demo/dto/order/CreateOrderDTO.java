package com.example.demo.dto.order;

import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CreateOrderDTO {
    private List<Long> seatIds;
    private String payType;
    private long amount;
    private String orderInfo;
}
