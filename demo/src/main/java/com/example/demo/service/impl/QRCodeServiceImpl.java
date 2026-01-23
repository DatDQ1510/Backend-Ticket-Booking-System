package com.example.demo.service.impl;

import com.example.demo.context.UserContext;
import com.example.demo.dto.ticket.TicketQRDataDTO;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.QRCodeService;
import com.example.demo.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeServiceImpl implements QRCodeService {

    private final TicketRepository ticketRepository;

    @Override
    public String generateTicketQRCode(Long ticketId) {
        // Lấy thông tin ticket từ database
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        
        // Verify user owns this ticket
        Long currentUserId = UserContext.getCurrentUserId();
        Long ticketUserId = ticket.getOrder().getUser().getUserId();
        
        if (!ticketUserId.equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: Ticket does not belong to current user");
        }
        
        return generateQRCodeInternal(ticket, ticketUserId);
    }
    
    /**
     * Generate QR code without user authorization check
     * For internal system use (e.g., EmailConsumer, Admin)
     */
    public String generateTicketQRCodeForSystem(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        
        Long ticketUserId = ticket.getOrder().getUser().getUserId();
        log.info("🎫 [SYSTEM] Generated QR code for ticket: {} (User: {})", ticketId, ticketUserId);
        
        return generateQRCodeInternal(ticket, ticketUserId);
    }
    
    /**
     * Internal method to generate QR code from ticket entity
     */
    private String generateQRCodeInternal(TicketEntity ticket, Long userId) {
        // Tạo QR data với timestamp để tránh giả mạo
        String qrData = String.format(
            "TICKET_ID:%d|ORDER_ID:%d|USER_ID:%d|EVENT_ID:%d|SEAT_ID:%d|EMAIL:%s|TIMESTAMP:%d",
            ticket.getTicketId(),
            ticket.getOrder().getOrderId(),
            userId,
            ticket.getEvent().getEventId(),
            ticket.getSeat().getSeatId(),
            ticket.getOrder().getUser().getEmail(),
            System.currentTimeMillis()
        );
        
        log.info("🎫 Generated QR code for ticket: {} (User: {})", ticket.getTicketId(), userId);
        
        // Generate QR code và return base64 string
        byte[] qrCodeImage = QRCodeUtil.generateQRCodeImage(qrData, 300, 300);
        return Base64.getEncoder().encodeToString(qrCodeImage);
    }

    @Override
    public String generateQRCode(String qrData, int width, int height) {
        byte[] qrCodeImage = QRCodeUtil.generateQRCodeImage(qrData, width, height);
        return Base64.getEncoder().encodeToString(qrCodeImage);
    }

    @Override
    public TicketQRDataDTO parseQRData(String qrData) {
        try {
            // Parse format: "TICKET_ID:123|ORDER_ID:456|USER_ID:789|..."
            String[] parts = qrData.split("\\|");
            TicketQRDataDTO.TicketQRDataDTOBuilder builder = TicketQRDataDTO.builder();
            
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length != 2) continue;
                
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                switch (key) {
                    case "TICKET_ID":
                        builder.ticketId(Long.parseLong(value));
                        break;
                    case "ORDER_ID":
                        builder.orderId(Long.parseLong(value));
                        break;
                    case "USER_ID":
                        builder.userId(Long.parseLong(value));
                        break;
                    case "EVENT_ID":
                        builder.eventId(Long.parseLong(value));
                        break;
                    case "SEAT_ID":
                        builder.seatId(Long.parseLong(value));
                        break;
                    case "EMAIL":
                        builder.email(value);
                        break;
                    case "TIMESTAMP":
                        builder.timestamp(Long.parseLong(value));
                        break;
                }
            }
            
            return builder.build();
        } catch (Exception e) {
            log.error("❌ Failed to parse QR data: {}", qrData, e);
            throw new RuntimeException("Invalid QR code format");
        }
    }

    @Override
    public boolean verifyQRCode(String qrData) {
        try {
            TicketQRDataDTO qrDataDTO = parseQRData(qrData);
            
            // Verify ticket exists và thuộc về đúng user
            TicketEntity ticket = ticketRepository.findById(qrDataDTO.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            
            // Verify user ID
            if (!ticket.getOrder().getUser().getUserId().equals(qrDataDTO.getUserId())) {
                log.warn("⚠️ QR verification failed: User ID mismatch");
                return false;
            }
            
            // Verify order ID
            if (!ticket.getOrder().getOrderId().equals(qrDataDTO.getOrderId())) {
                log.warn("⚠️ QR verification failed: Order ID mismatch");
                return false;
            }
            
            // Verify event ID
            if (!ticket.getEvent().getEventId().equals(qrDataDTO.getEventId())) {
                log.warn("⚠️ QR verification failed: Event ID mismatch");
                return false;
            }
            
            // Verify timestamp (QR code valid for 24 hours)
            if (qrDataDTO.getTimestamp() != null) {
                long hoursSinceCreation = (System.currentTimeMillis() - qrDataDTO.getTimestamp()) / (1000 * 60 * 60);
                if (hoursSinceCreation > 24) {
                    log.warn("⚠️ QR verification failed: QR code expired (created {} hours ago)", hoursSinceCreation);
                    return false;
                }
            }
            
            log.info("✅ QR code verified successfully for ticket: {}", qrDataDTO.getTicketId());
            return true;
            
        } catch (Exception e) {
            log.error("❌ QR verification failed: {}", e.getMessage());
            return false;
        }
    }
}
