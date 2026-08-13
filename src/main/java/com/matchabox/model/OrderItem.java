package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    @Column(nullable = false)
    private int quantity = 1;
    @Column(name = "unit_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Column(length = 20)
    private String size;
    @Column(name = "milk_type", length = 30)
    private String milkType;
    @Column(length = 20)
    private String temperature;
    @Column(name = "sweetness_level")
    private int sweetnessLevel = 50;
    @Column(name = "special_request", length = 300)
    private String specialRequest;
    @Column(name = "is_done", nullable = false)
    private boolean isDone = false;
    public OrderItem() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order o) { this.order = o; }
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long m) { this.menuItemId = m; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal p) { this.unitPrice = p; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal p) { this.totalPrice = p; }
    public String getSize() { return size; }
    public void setSize(String s) { this.size = s; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String m) { this.milkType = m; }
    public String getTemperature() { return temperature; }
    public void setTemperature(String t) { this.temperature = t; }
    public int getSweetnessLevel() { return sweetnessLevel; }
    public void setSweetnessLevel(int s) { this.sweetnessLevel = s; }
    public String getSpecialRequest() { return specialRequest; }
    public void setSpecialRequest(String s) { this.specialRequest = s; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean d) { isDone = d; }
}
