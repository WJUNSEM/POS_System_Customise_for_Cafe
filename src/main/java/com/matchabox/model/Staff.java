package com.matchabox.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "staff")
public class Staff {
    public enum Role { BARISTA, CASHIER, KITCHEN, SUPERVISOR, MANAGER }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max=100) @Column(nullable=false,length=100)
    private String name;
    @NotBlank @Email @Column(nullable=false,unique=true,length=150)
    private String email;
    @Column(length=20)
    private String phone;
    @NotBlank @Pattern(regexp="\\d{4}") @Column(nullable=false,length=4)
    @JsonIgnore
    private String pin;
    @NotBlank @Column(name="password_hash",nullable=false,length=255)
    @JsonIgnore
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
    private Role role = Role.BARISTA;
    @Column(name="is_active",nullable=false)
    private boolean isActive = true;
    @OneToMany(mappedBy="staff",cascade=CascadeType.ALL,fetch=FetchType.LAZY,orphanRemoval=true)
    @JsonIgnore
    private List<ClockRecord> clockRecords;
    @OneToMany(mappedBy="staff",fetch=FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;
    @Column(name="created_at",updatable=false)
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Staff() {}
    public Staff(String name, String email, String pin, String passwordHash, Role role) {
        this.name=name; this.email=email; this.pin=pin; this.passwordHash=passwordHash; this.role=role;
    }
    public String getInitials() {
        if (name==null||name.isBlank()) return "?";
        String[] p=name.trim().split("\\s+");
        if (p.length==1) return p[0].substring(0,1).toUpperCase();
        return (p[0].substring(0,1)+p[p.length-1].substring(0,1)).toUpperCase();
    }
    public String getFirstName() {
        if (name==null||name.isBlank()) return "";
        return name.trim().split("\\s+")[0];
    }
    public boolean isManager() { return role==Role.MANAGER||role==Role.SUPERVISOR; }
    public boolean verifyPin(String inputPin) { return this.pin!=null&&this.pin.equals(inputPin); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public String getName() { return name; }
    public void setName(String n) { this.name=n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email=e; }
    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone=p; }
    public String getPin() { return pin; }
    public void setPin(String p) { this.pin=p; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash=h; }
    public Role getRole() { return role; }
    public void setRole(Role r) { this.role=r; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean a) { isActive=a; }
    public List<ClockRecord> getClockRecords() { return clockRecords; }
    public void setClockRecords(List<ClockRecord> c) { this.clockRecords=c; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> o) { this.orders=o; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override public String toString() { return "Staff{id="+id+", name='"+name+"', role="+role+"}"; }
}
