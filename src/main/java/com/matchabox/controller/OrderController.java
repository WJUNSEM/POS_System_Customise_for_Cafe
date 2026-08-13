package com.matchabox.controller;
import com.matchabox.model.*;
import com.matchabox.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import java.time.*;
import java.util.*;
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private StaffRepository staffRepository;
    @Value("${app.order.service-charge-percent:6}") private double serviceCharge;
    @Value("${app.loyalty.points-per-order:1}")     private int pointsPerOrder;

    @GetMapping
    public ResponseEntity<List<Order>> getAll(@RequestParam(required=false) String status, @RequestParam(required=false) String date) {
        try {
            List<Order> orders;
            if (status != null && !status.isBlank()) {
                orders = orderRepository.findByStatusOrderByCreatedAtAsc(Order.Status.valueOf(status.toUpperCase()));
            } else if (date != null && !date.isBlank()) {
                orders = orderRepository.findByShiftDate(LocalDate.parse(date));
            } else {
                orders = orderRepository.findAllByOrderByCreatedAtDesc();
            }
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActive() {
        return ResponseEntity.ok(orderRepository.findByStatusInOrderByCreatedAtAsc(List.of(Order.Status.NEW,Order.Status.PREPARING)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/number/{num}")
    public ResponseEntity<Order> getByNumber(@PathVariable String num) {
        return orderRepository.findByOrderNumber(num).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/customer/{cid}")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable Long cid) {
        return ResponseEntity.ok(orderRepository.findByCustomerIdOrderByCreatedAtDesc(cid));
    }
    @GetMapping("/stats/today")
    public ResponseEntity<Map<String,Object>> getStats() {
        String today=LocalDate.now().toString();
        List<Order> orders=orderRepository.findByShiftDate(LocalDate.now());
        double rev=orders.stream().filter(o->o.getStatus()==Order.Status.COMPLETED).mapToDouble(o->o.getTotalAmount().doubleValue()).sum();
        return ResponseEntity.ok(Map.of("totalOrders",orders.size(),"activeOrders",orders.stream().filter(Order::isActive).count(),"revenue",BigDecimal.valueOf(rev).setScale(2,RoundingMode.HALF_UP),"date",today));
    }
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String,Object>> create(@RequestBody Map<String,Object> body) {
        try {
            Order order=new Order();
            if (body.get("customerId")!=null) customerRepository.findById(Long.valueOf(body.get("customerId").toString())).ifPresent(order::setCustomer);
            if (body.get("staffId")!=null) staffRepository.findById(Long.valueOf(body.get("staffId").toString())).ifPresent(order::setStaff);
            order.setOrderType(Order.OrderType.valueOf(body.getOrDefault("orderType","dine_in").toString().toUpperCase()));
            if (body.get("tableNumber")!=null) order.setTableNumber(body.get("tableNumber").toString());
            if (body.get("specialNotes")!=null) order.setSpecialNotes(body.get("specialNotes").toString());
            if (body.get("promoCode")!=null) order.setPromoCode(body.get("promoCode").toString());
            order.setOrderNumber(generateOrderNumber());
            order.setStatus(Order.Status.NEW);
            @SuppressWarnings("unchecked") List<Map<String,Object>> items=(List<Map<String,Object>>)body.get("items");
            if (items==null||items.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","Order must have at least one item"));
            double sub=0; for (Map<String,Object> i:items) sub+=Double.parseDouble(i.get("unitPrice").toString())*Integer.parseInt(i.getOrDefault("quantity",1).toString());
            double svc=BigDecimal.valueOf(sub).multiply(BigDecimal.valueOf(serviceCharge/100)).setScale(2,RoundingMode.HALF_UP).doubleValue();
            double disc=body.get("discountAmount")!=null?Double.parseDouble(body.get("discountAmount").toString()):0;
            double loyal=body.get("loyaltyUsed")!=null?Double.parseDouble(body.get("loyaltyUsed").toString()):0;
            double total=Math.max(0,sub+svc-disc-loyal);
            order.setSubtotal(BigDecimal.valueOf(sub)); order.setServiceCharge(BigDecimal.valueOf(svc));
            order.setDiscountAmount(BigDecimal.valueOf(disc)); order.setLoyaltyUsed(BigDecimal.valueOf(loyal));
            order.setTotalAmount(BigDecimal.valueOf(total).setScale(2,RoundingMode.HALF_UP));
            Order saved=orderRepository.save(order);
            for (Map<String,Object> i:items) {
                OrderItem oi=new OrderItem(); oi.setOrder(saved);
                oi.setMenuItemId(Long.valueOf(i.get("menuItemId").toString()));
                oi.setQuantity(Integer.parseInt(i.getOrDefault("quantity",1).toString()));
                oi.setUnitPrice(BigDecimal.valueOf(Double.parseDouble(i.get("unitPrice").toString())));
                oi.setTotalPrice(oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity())));
                if (i.get("size")!=null) oi.setSize(i.get("size").toString());
                if (i.get("milkType")!=null) oi.setMilkType(i.get("milkType").toString());
                if (i.get("temperature")!=null) oi.setTemperature(i.get("temperature").toString());
                if (i.get("sweetnessLevel")!=null) oi.setSweetnessLevel(Integer.parseInt(i.get("sweetnessLevel").toString()));
                if (i.get("specialRequest")!=null) oi.setSpecialRequest(i.get("specialRequest").toString());
                orderItemRepository.save(oi);
            }
            if (saved.getCustomer()!=null) { Customer c=saved.getCustomer(); c.setLoyaltyPoints(c.getLoyaltyPoints()+pointsPerOrder); c.setTotalOrders(c.getTotalOrders()+1); c.setTotalSpent(c.getTotalSpent().add(saved.getTotalAmount())); customerRepository.save(c); }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId",saved.getId(),"orderNumber",saved.getOrderNumber(),"status",saved.getStatus(),"totalAmount",saved.getTotalAmount(),"message","Order placed successfully!"));
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Failed: "+e.getMessage())); }
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String,Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return orderRepository.findById(id).map(order -> {
            try { order.setStatus(Order.Status.valueOf(body.get("status").toUpperCase())); orderRepository.save(order);
                return ResponseEntity.ok(Map.<String,Object>of("orderId",order.getId(),"status",order.getStatus(),"message","Order "+order.getOrderNumber()+" updated"));
            } catch (Exception e) { return ResponseEntity.badRequest().body(Map.<String,Object>of("error","Invalid status")); }
        }).orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/{orderId}/items/{itemId}/done")
    public ResponseEntity<Map<String,Object>> markDone(@PathVariable Long orderId, @PathVariable Long itemId, @RequestBody Map<String,Boolean> body) {
        return orderItemRepository.findById(itemId).map(item -> {
            item.setDone(body.getOrDefault("done",true)); orderItemRepository.save(item);
            return ResponseEntity.ok(Map.<String,Object>of("itemId",item.getId(),"done",item.isDone()));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String,Object>> cancel(@PathVariable Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus()!=Order.Status.NEW) return ResponseEntity.badRequest().body(Map.<String,Object>of("error","Cannot cancel: "+order.getStatus()));
            order.setStatus(Order.Status.CANCELLED); orderRepository.save(order);
            return ResponseEntity.ok(Map.<String,Object>of("message","Order "+order.getOrderNumber()+" cancelled","status",order.getStatus()));
        }).orElse(ResponseEntity.notFound().build());
    }
    private String generateOrderNumber() {
        String number;
        do {
            number = String.format("#%04d", (System.currentTimeMillis() / 1000) % 10000);
        } while (orderRepository.findByOrderNumber(number).isPresent());
        return number;
    }

}
