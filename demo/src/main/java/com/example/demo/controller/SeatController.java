package com.example.demo.controller;


import com.esotericsoftware.minlog.Log;
import com.example.demo.dto.seat.CreateSeatDTO;
import com.example.demo.dto.seat.SeatDTO;
import com.example.demo.dto.seat.UpdateSeatDTO;
import com.example.demo.entity.SeatEntity;
import com.example.demo.mapper.SeatMapper;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatMapper seatMapper;

    @PostMapping("/create")
    public ApiResponse<List<SeatDTO>> createSeat(@RequestBody CreateSeatDTO seatDTO) {
        System.out.println("Received CreateSeatDTO: " + seatDTO);
        List<SeatEntity>  seatEntities = seatService.createSeats(seatDTO);
        List<SeatDTO> response =  seatMapper.toUpdatedList(seatEntities);
        return ApiResponse.success("Tạo danh sách chỗ ngồi cho sự kiện thành công", response);
    }

    @PatchMapping("/{seatId}")
    public ApiResponse<List<SeatDTO>> updateSeat(@RequestBody UpdateSeatDTO updateSeatDTO) {
        Long[] seatIds = updateSeatDTO.getSeatIds();
        String status = updateSeatDTO.getStatus();
        List<SeatEntity> seatEntities = seatService.updateSeats(seatIds, status);
        List<SeatDTO> response =  seatMapper.toUpdatedList(seatEntities);
        return ApiResponse.success("Cập nhật trạng thái chỗ ngồi thành công", response);
    }

    @GetMapping("/event/{eventId}")
    public ApiResponse<List<SeatDTO>> getSeatsByEvent(@PathVariable Long eventId) {
        List<SeatEntity> seatEntities = seatService.getSeatsByEventId(eventId);
        List<SeatDTO> response = seatMapper.toUpdatedList(seatEntities);
        return ApiResponse.success("Lấy danh sách chỗ ngồi thành công", response);
    }

    @GetMapping("/event/{eventId}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeatsByEvent(@PathVariable Long eventId) {
        List<SeatEntity> seatEntities = seatService.getAvailableSeatsByEventId(eventId);
        List<SeatDTO> response = seatMapper.toUpdatedList(seatEntities);
        return ApiResponse.success("Lấy danh sách chỗ ngồi còn trống thành công", response);
    }
}
