package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
@Entity
@Table(name = "menu_categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(name = "display_order")
    private int sortOrder = 0;
    @Column(name = "is_active")
    private boolean isActive = true;
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MenuItem> menuItems;
    public Category() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int s) { this.sortOrder = s; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean a) { isActive = a; }
}
