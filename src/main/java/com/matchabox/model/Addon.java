package com.matchabox.model;
import jakarta.persistence.*;
@Entity
@Table(name = "menu_addons")
public class Addon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "price", nullable = false)
    private double extraPrice = 0.0;
    @Column(name = "is_available")
    private boolean isAvailable = true;
    public Addon() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public double getExtraPrice() { return extraPrice; }
    public void setExtraPrice(double p) { this.extraPrice = p; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean a) { isAvailable = a; }
}
