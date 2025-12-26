package com.example.demo.service.impl;

import com.example.demo.context.UserContext;
import com.example.demo.dto.order.CreateOrderDTO;
import com.example.demo.dto.order.OrderResponse;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.SeatStatus;
import com.example.demo.entity.enums.TicketStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final MomoService momoService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderDTO orderDTO) {
        Long userId = UserContext.getCurrentUserId();
        List<Long> seatIds = orderDTO.getSeatIds();
        String payType = orderDTO.getPayType();

        // 1️⃣ Lấy tất cả seat trong 1 query duy nhất
        List<SeatEntity> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        // 2️⃣ Kiểm tra seat hợp lệ và cập nhật trạng thái
        Long totalAmount = 0L;
        for (SeatEntity seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
            seat.setStatus(SeatStatus.BOOKED);
            totalAmount += seat.getPrice();
        }

        // 3️⃣ Cập nhật seat đồng loạt
        seatRepository.saveAll(seats);

        // 4️⃣ Lấy reference thay vì findById để không tốn query
        UserEntity userRef = userRepository.getReferenceById(userId);

        // 5️⃣ Tạo Order (chưa cần save ngay)
        OrderEntity order = OrderEntity.builder()
                .user(userRef)
                .amount(totalAmount)
                .payType(payType)
                .status(OrderStatus.PENDING)
                .paidAt(null)
                .build();

        // 6️⃣ Tạo ticket list (use Collectors.toList() for mutable list)
        List<TicketEntity> tickets = seats.stream()
                .map(seat -> TicketEntity.builder()
                        .seat(seat)
                        .order(order)
                        .event(seat.getEvent())
                        .status(TicketStatus.RESERVED)                        .build())
                .collect(Collectors.toList()); // Mutable ArrayList for Hibernate

        order.setTickets(tickets);

        // 7️⃣ Save order (cascade sẽ tự save ticket)
        orderRepository.save(order);

        // 8️⃣ Create payment with MomoService and get payUrl
        String payUrl = null;
        try {
            Map<String, Object> paymentResponse = momoService.createPayment(
                    totalAmount,
                    String.valueOf(order.getOrderId()),
                    "Thanh toán vé sự kiện #" + order.getOrderId()
            );
            System.out.println("paymentResponse" + paymentResponse);
            payUrl = (String) paymentResponse.get("payUrl");
            
            // Update order fields - Hibernate will auto-detect changes (dirty checking)
            order.setMomoTransId(payUrl);
            order.setStatus(OrderStatus.WAITING_PAYMENT);
            // No need to call save() again - transaction will auto-update
            
            System.out.println("✅ Created payment for order " + order.getOrderId() + " → " + payUrl);
        } catch (Exception e) {
            System.err.println("❌ Error creating payment for order " + order.getOrderId());
            e.printStackTrace();
            // Keep order in PENDING status if payment creation fails
        }

        // 9️⃣ Convert Entity to DTO and include payUrl for frontend redirect
        return mapToOrderResponse(order, payUrl);
    }
    
    private OrderResponse mapToOrderResponse(OrderEntity order, String payUrl) {
        List<OrderResponse.TicketResponse> ticketResponses = order.getTickets().stream()
            .map(ticket -> OrderResponse.TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .status(ticket.getStatus())
                .seatId(ticket.getSeat().getSeatId())
                .seatNumber(ticket.getSeat().getSeatNumber())
                .seatRow(ticket.getSeat().getSeatRow())
                .seatType(ticket.getSeat().getSeatType())
                .price(ticket.getSeat().getPrice())
                .eventId(ticket.getEvent().getEventId())
                .eventTitle(ticket.getEvent().getTitle())
                .build())
            .collect(Collectors.toList());
        
        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .userId(order.getUser().getUserId())
            .amount(order.getAmount())
            .payType(order.getPayType())
            .status(order.getStatus())
            .momoTransId(order.getMomoTransId())
            .payUrl(payUrl) // Include payment URL for frontend redirect
            .paidAt(order.getPaidAt())
            .tickets(ticketResponses)
            .build();
    }

    @Override
    public OrderEntity updateOrder(Long orderId, int resultCode) {
        return null;
    }

    @Override
    @Transactional
    public void updateOrderPaymentSuccess(Long orderId, String transId) {
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // check idempotency
        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println("⚠️ Order " + orderId + " is already PAID. Skipping update.");
            return;
        }
        // Cập nhật order
        order.setStatus(OrderStatus.PAID);
        order.setMomoTransId(transId);
        order.setPaidAt(java.time.LocalDateTime.now());
        System.out.println("order" + order);
        // Cập nhật tickets sang SOLD
        for (TicketEntity ticket : order.getTickets()) {
            ticket.setStatus(TicketStatus.SOLD);
        }
        
        orderRepository.save(order);
        System.out.println("✅ Updated order " + orderId + " to PAID status");
    }

    @Override
    @Transactional
    public void updateOrderPaymentFailed(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // Cập nhật order
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        
        // Giải phóng ghế (set lại AVAILABLE)
        for (TicketEntity ticket : order.getTickets()) {
            SeatEntity seat = ticket.getSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            ticket.setStatus(TicketStatus.AVAILABLE);
        }
        
        orderRepository.save(order);
        System.out.println("❌ Updated order " + orderId + " to PAYMENT_FAILED status and released seats");
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserUserIdOrderByOrderIdDesc(userId);
        
        return orders.stream()
            .map(order -> mapToOrderResponse(order, null))
            .collect(Collectors.toList());
    }

}
