package com.example.demo.service.impl;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.context.UserContext;
import com.example.demo.dto.PaymentNotificationDTO;
import com.example.demo.dto.order.CreateOrderDTO;
import com.example.demo.dto.order.OrderResponse;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.SeatStatus;
import com.example.demo.entity.enums.TicketStatus;
import com.example.demo.payload.ApiResponse;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final MomoService momoService;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

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
        log.info("seats: " + seats);
        // 2️⃣ Kiểm tra seat hợp lệ và cập nhật trạng thái sang HOLD (giữ chỗ 15 phút)
        Long totalAmount = 0L;
        for (SeatEntity seat : seats) {
            log.info("Checking seat: {} - Status: {}", seat.getSeatNumber(), seat.getStatus());
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Ghế " + seat.getSeatNumber() + " đang được giữ hoặc đã được đặt. Trạng thái: " + seat.getStatus());
            }
            seat.setStatus(SeatStatus.HOLD);  // HOLD thay vì BOOKED
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
            orderRepository.save(order);
            
//            // Send event to RabbitMQ for email notification when payment succeeds
//            try {
//                PaymentNotificationDTO notification = new PaymentNotificationDTO(
//                    order.getOrderId(),
//                    0, // resultCode = 0 (success) - sẽ được update lại khi MoMo callback
//                    null, // transId - chưa có, đợi callback
//                    totalAmount,
//                    "Thanh toán vé sự kiện #" + order.getOrderId(),
//                    payType,
//                    null, null, null
//                );
//                rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, notification);
//                log.info("📨 Sent payment notification to RabbitMQ for order: {}", order.getOrderId());
//            } catch (Exception rabbitEx) {
//                log.error("❌ Failed to send message to RabbitMQ for order: {}", order.getOrderId(), rabbitEx);
//                // Don't throw - continue with order creation even if RabbitMQ fails
//            }
            
            System.out.println("✅ Created payment for order " + order.getOrderId() + " → " + payUrl);
        } catch (Exception e) {
            System.err.println("❌ Error creating payment for order " + order.getOrderId());
            e.printStackTrace();
            // Keep order in PENDING status if payment creation fails
        }

        // 9️⃣ Convert Entity to DTO and include payUrl for frontend redirect
        return mapToOrderResponse(order, payUrl);
    }


    @Override
    public OrderResponse tryKeyLock(CreateOrderDTO orderDTO) {
        // 1. Tạo key lock dựa trên danh sách ID ghế đã sắp xếp (để tránh deadlock)
        List<Long> sortedIds = orderDTO.getSeatIds().stream().sorted().toList();
        String lockKey = "lock:seats:" + sortedIds;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. Thử chiếm lock trong 5 giây, giữ tối đa 10 giây
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 3. Gọi method có @Transactional ở Service khác
                log.info("Acquired lock for key: " + lockKey);
                return this.createOrder(orderDTO);
            } else {
                throw new RuntimeException("Ghế đang được người khác giữ, vui lòng thử lại sau.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lỗi hệ thống khi xử lý khóa.");
        } finally {
            // 4. Luôn nhả lock sau khi Transaction đã hoàn tất (COMMIT)
            // Kiểm tra xem lock có đang được giữ bởi thread hiện tại không trước khi unlock
            if (lock.isHeldByCurrentThread()) {
                log.info("Unlock {}: ", lockKey);
                lock.unlock();
            }
        }
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
        
        // Cập nhật tickets sang SOLD và seats từ HOLD sang BOOKED
        for (TicketEntity ticket : order.getTickets()) {
            ticket.setStatus(TicketStatus.SOLD);
            SeatEntity seat = ticket.getSeat();
            seat.setStatus(SeatStatus.BOOKED);  // HOLD → BOOKED
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

    @Override
    public ApiResponse<?> getRevenueDashboardData() {

        LocalDate today = LocalDate.now();

        // Tháng này
        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);

        // Tháng trước (cùng kỳ)
        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).atTime(23, 59, 59);

        return ApiResponse.success("Get full data revenue Dashboard", orderRepository.getOrderStatsMTD(
                startThisMonth,
                endThisMonth,
                startLastMonth,
                endLastMonth
        ));
    }

}
