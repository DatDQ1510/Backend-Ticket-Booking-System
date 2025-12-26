package com.example.demo.mapper;

import com.example.demo.dto.order.CreateOrderDTO;
import com.example.demo.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    CreateOrderDTO toResponse(OrderEntity orderEntity);
}
