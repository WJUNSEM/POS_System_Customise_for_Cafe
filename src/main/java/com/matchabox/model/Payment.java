package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "payments")
public class Payment {
    public enum Method { FPX, EWALLET, CARD, CASH }
    public enum Status { PENDING, PAID, FAILED, REFUNDED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method method;
    @Column(length = 50)
    private String provider;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;
    @Column(name = "transaction_ref", length = 100)
    private String referenceNo;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
    public Payment() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order o) { this.order = o; }
    public Method getMethod() { return method; }
    public void setMethod(Method m) { this.method = m; }
    public String getProvider() { return provider; }
    public void setProvider(String p) { this.provider = p; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal a) { this.amount = a; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String r) { this.referenceNo = r; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime p) { this.paidAt = p; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
