package com.matchabox.controller;
import com.matchabox.model.Order;
import com.matchabox.model.Payment;
import com.matchabox.repository.OrderRepository;
import com.matchabox.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderRepository orderRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(@PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String,Object>> createPayment(@RequestBody Map<String,Object> body) {
        try{
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();
            if (paymentRepository.findByOrderId(orderId).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Payment already exists for this order"));
            }
            Payment payment = new Payment();
            payment.setOrder(orderOpt.get());
            payment.setMethod(Payment.Method.valueOf(body.get("method").toString().toUpperCase()));
            if (body.get("provider")!=null) payment.setProvider(body.get("provider").toString());
            payment.setAmount(BigDecimal.valueOf(Double.parseDouble(body.get("amount").toString())));
            if (body.get("transactionRef") != null) payment.setReferenceNo(body.get("transactionRef").toString());
            payment.setStatus(Payment.Status.PAID);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Payment recorded","status","PAID"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Failed: "+e.getMessage()));
        }
    }
}
