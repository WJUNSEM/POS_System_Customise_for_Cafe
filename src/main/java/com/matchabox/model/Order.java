package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "orders")
public class Order {
    public enum Status { NEW, PREPARING, READY, COMPLETED, CANCELLED }
    public enum OrderType { DINE_IN, TAKEAWAY }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnoreProperties({"orders","passwordHash"})
    private Customer customer;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = true)
    @JsonIgnoreProperties({"passwordHash","pin","clockRecords","orders"})
    private Staff staff;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnoreProperties("order")
    private Payment payment;
    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType = OrderType.DINE_IN;
    @Column(name = "table_number", length = 10)
    private String tableNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;
    @Column(name = "notes", length = 255)
    private String specialNotes;
    @Column(name = "promo_code", length = 50)
    private String promoCode;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "service_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceCharge = BigDecimal.ZERO;
    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "loyalty_used", nullable = false, precision = 10, scale = 2)
    private BigDecimal loyaltyUsed = BigDecimal.ZERO;
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Order() {}
    public boolean isCancellable() { return this.status == Status.NEW; }
    public boolean isActive() { return status == Status.NEW || status == Status.PREPARING; }
    public int getTotalItemCount() { if (items==null) return 0; return items.stream().mapToInt(OrderItem::getQuantity).sum(); }
    public boolean isFullyPrepared() { if (items==null||items.isEmpty()) return false; return items.stream().allMatch(OrderItem::isDone); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer c) { this.customer = c; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff s) { this.staff = s; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> i) { this.items = i; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment p) { this.payment = p; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String n) { this.orderNumber = n; }
    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType t) { this.orderType = t; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String t) { this.tableNumber = t; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String n) { this.specialNotes = n; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String p) { this.promoCode = p; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal s) { this.subtotal = s; }
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal s) { this.serviceCharge = s; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal d) { this.discountAmount = d; }
    public BigDecimal getLoyaltyUsed() { return loyaltyUsed; }
    public void setLoyaltyUsed(BigDecimal l) { this.loyaltyUsed = l; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal t) { this.totalAmount = t; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override public String toString() { return "Order{id="+id+", number='"+orderNumber+"', status="+status+", total="+totalAmount+"}"; }
}
