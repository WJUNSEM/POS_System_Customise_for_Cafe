package com.matchabox.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties("menuItems")
    private Category category;
    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 500)
    private String description;
    @NotNull @Column(name = "base_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal basePrice;
    @Column(name = "image_url", length = 300)
    private String imageUrl;
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;
    @Column(name = "is_popular", nullable = false)
    private boolean isPopular = false;
    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;
    @Column
    private Integer calories;

    @Column(name = "display_order", nullable = false)
    private int sortOrder = 0;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @JsonIgnoreProperties("menuItem")
    private List<ItemOption> options;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public MenuItem() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category c) { this.category = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal p) { this.basePrice = p; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String u) { this.imageUrl = u; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean a) { isAvailable = a; }
    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean p) { isPopular = p; }
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean f) { isFeatured = f; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer c) { this.calories = c; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int s) { this.sortOrder = s; }
    public List<ItemOption> getOptions() { return options; }
    public void setOptions(List<ItemOption> o) { this.options = o; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    @Override public String toString() { return "MenuItem{id="+id+", name='"+name+"', price="+basePrice+"}"; }
}
