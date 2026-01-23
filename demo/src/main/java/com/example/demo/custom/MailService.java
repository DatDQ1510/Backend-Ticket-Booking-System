package com.example.demo.custom;



import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.QRCodeService;
import com.example.demo.service.TicketService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.util.Base64;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;
    private final TicketService ticketService;
    private final QRCodeService qrCodeService;

    public void sendOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("OTP for Resetting Password");
        message.setText("Your OTP code is: " + otp + ". It is valid for 15 minutes.");

        mailSender.send(message);
    }

    public void sendPaymentSuccessEmail(String email, Long orderId, double amount) {
        try {
            // Fetch order với tickets, events, và seats (eager loading để tránh LazyInitializationException)
            OrderEntity order = orderRepository.findByIdWithTicketsAndDetails(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Get all tickets for this order
            List<TicketEntity> tickets = order.getTickets();

            // Create email with HTML format
            MimeMessage message = mailSender.createMimeMessage();

            // multi-part message = true allows sending email with attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Payment Successful - Your Tickets");

            // Build HTML email content
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("<html><body style='font-family: Arial, sans-serif;'>");
            emailContent.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
            emailContent.append("<h2 style='color: #4CAF50;'>Payment Successful!</h2>");
            emailContent.append("<p>Thank you for your purchase. Your payment has been processed successfully.</p>");
            emailContent.append("<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
            emailContent.append("<h3>Order Details</h3>");
            emailContent.append("<p><strong>Order ID:</strong> ").append(orderId).append("</p>");
            emailContent.append("<p><strong>Total Amount:</strong> ").append(String.format("%.2f", amount)).append(" VND</p>");
            
            if (order.getPaidAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                emailContent.append("<p><strong>Payment Date:</strong> ").append(order.getPaidAt().format(formatter)).append("</p>");
            }
            
            emailContent.append("<p><strong>Number of Tickets:</strong> ").append(tickets.size()).append("</p>");
            emailContent.append("</div>");

            emailContent.append("<h3>Your Tickets</h3>");
            emailContent.append("<p>Please find your tickets attached as QR codes. Present these QR codes at the venue for entry.</p>");

            // Generate and attach QR code for each ticket
            for (int i = 0; i < tickets.size(); i++) {
                TicketEntity ticket = tickets.get(i);
                
                // Generate QR code using system method (no user authentication required for email)
                String qrCodeBase64 = qrCodeService.generateTicketQRCodeForSystem(ticket.getTicketId());
                byte[] qrCodeImage = Base64.getDecoder().decode(qrCodeBase64);

                // Attach QR code to email
                String attachmentName = "ticket_" + (i + 1) + "_qr_code.png";
                helper.addAttachment(attachmentName, () -> new ByteArrayInputStream(qrCodeImage), "image/png");

                emailContent.append("<div style='border: 1px solid #ddd; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
                emailContent.append("<p><strong>Ticket #").append(i + 1).append("</strong></p>");
                emailContent.append("<p>Ticket ID: ").append(ticket.getTicketId()).append("</p>");
                emailContent.append("<p>Event: ").append(ticket.getEvent().getTitle()).append("</p>");
                emailContent.append("<p>Seat: ").append(ticket.getSeat().getSeatNumber()).append("</p>");
                emailContent.append("<p><em>QR Code attached: ").append(attachmentName).append("</em></p>");
                emailContent.append("</div>");
            }

            emailContent.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>");
            emailContent.append("<p style='color: #666; font-size: 12px;'>If you have any questions, please contact our support team.</p>");
            emailContent.append("<p style='color: #666; font-size: 12px;'>This is an automated email. Please do not reply.</p>");
            emailContent.append("</div>");
            emailContent.append("</div></body></html>");

            helper.setText(emailContent.toString(), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send payment success email: " + e.getMessage(), e);
        }
    }
}
