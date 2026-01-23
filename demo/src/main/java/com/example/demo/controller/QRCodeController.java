package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.ticket.TicketQRDataDTO;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    /**
     * Get QR code for a specific ticket
     * GET /api/qrcode/ticket/{ticketId}
     */
    @GetMapping("/ticket/{ticketId}")
    public ApiResponse<String> getTicketQRCode(@PathVariable Long ticketId) {
        String qrCodeBase64 = qrCodeService.generateTicketQRCode(ticketId);
        return ApiResponse.success("QR code generated successfully", qrCodeBase64);
    }

    /**
     * Verify QR code data
     * POST /api/qrcode/verify
     */
    @PostMapping("/verify")
    public ApiResponse<TicketQRDataDTO> verifyQRCode(@RequestBody String qrData) {
        boolean isValid = qrCodeService.verifyQRCode(qrData);
        
        if (!isValid) {
            return ApiResponse.error("Invalid or expired QR code");
        }
        
        TicketQRDataDTO parsedData = qrCodeService.parseQRData(qrData);
        return ApiResponse.success("QR code verified successfully", parsedData);
    }
}
