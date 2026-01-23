package com.example.demo.service;

import com.example.demo.dto.ticket.TicketQRDataDTO;

public interface QRCodeService {
    
    /**
     * Generate QR code for a ticket
     * @param ticketId Ticket ID
     * @return Base64 encoded QR code image
     */
    String generateTicketQRCode(Long ticketId);
    
    /**
     * Generate QR code with custom data
     * @param qrData QR code data
     * @param width Width of QR code
     * @param height Height of QR code
     * @return Base64 encoded QR code image
     */
    String generateQRCode(String qrData, int width, int height);
    
    /**
     * Parse and validate QR code data
     * @param qrData Raw QR data string
     * @return Parsed ticket QR data
     */
    TicketQRDataDTO parseQRData(String qrData);
    
    /**
     * Verify QR code authenticity
     * @param qrData Raw QR data string
     * @return true if valid, false otherwise
     */
    boolean verifyQRCode(String qrData);
}
