package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "menu_item_options")
public class ItemOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    @JsonIgnore
    private MenuItem menuItem;
    @Column(name = "option_group", nullable = false, length = 50)
    private String optionGroup;
    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;
    @Column(name = "price_add", nullable = false)
    private double extraPrice = 0.0;
    @Column(name = "is_default")
    private boolean isDefault = false;
    @Column(name = "display_order")
    private int sortOrder = 0;
    public ItemOption() {}
    public Long getId() { return id; }
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem m) { this.menuItem = m; }
    public String getOptionGroup() { return optionGroup; }
    public void setOptionGroup(String g) { this.optionGroup = g; }
    public String getOptionName() { return optionName; }
    public void setOptionName(String n) { this.optionName = n; }
    public double getExtraPrice() { return extraPrice; }
    public void setExtraPrice(double p) { this.extraPrice = p; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean d) { isDefault = d; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int s) { this.sortOrder = s; }
}
