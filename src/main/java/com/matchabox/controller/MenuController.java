package com.matchabox.controller;
import com.matchabox.model.*;
import com.matchabox.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemOptionRepository itemOptionRepository;
    @Autowired private AddonRepository addonRepository;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllItems() {
        return ResponseEntity.ok(menuItemRepository.findByIsAvailableTrue());
    }
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getItemById(@PathVariable Long id) {
        return menuItemRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/category/{slug}")
    public ResponseEntity<List<MenuItem>> getByCategory(@PathVariable String slug) {
        Optional<Category> cat = categoryRepository.findBySlug(slug);
        if (cat.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(menuItemRepository.findByCategoryAndIsAvailableTrue(cat.get()));
    }
    @GetMapping("/featured")
    public ResponseEntity<List<MenuItem>> getFeatured() {
        return ResponseEntity.ok(menuItemRepository.findByIsFeaturedTrueAndIsAvailableTrue());
    }
    @GetMapping("/popular")
    public ResponseEntity<List<MenuItem>> getPopular() {
        return ResponseEntity.ok(menuItemRepository.findByIsPopularTrueAndIsAvailableTrue());
    }
    @GetMapping("/{id}/options")
    public ResponseEntity<Map<String, List<ItemOption>>> getOptions(@PathVariable Long id) {
        Optional<MenuItem> item = menuItemRepository.findById(id);
        if (item.isEmpty()) return ResponseEntity.notFound().build();
        List<ItemOption> all = itemOptionRepository.findByMenuItemOrderBySortOrderAsc(item.get());
        Map<String, List<ItemOption>> grouped = new HashMap<>();
        for (ItemOption opt : all) {
            grouped.computeIfAbsent(opt.getOptionGroup(), k -> new ArrayList<>()).add(opt);
        }
        return ResponseEntity.ok(grouped);
    }
    @GetMapping("/addons")
    public ResponseEntity<List<Addon>> getAddons() {
        return ResponseEntity.ok(addonRepository.findByIsAvailableTrue());
    }
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc());
    }
    @PostMapping
    public ResponseEntity<MenuItem> createItem(@RequestBody MenuItem item) {
        if (item.getCategory() == null || item.getCategory().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Category> category = categoryRepository.findById(item.getCategory().getId());
        if (category.isEmpty()) return ResponseEntity.badRequest().build();
        item.setCategory(category.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemRepository.save(item));
    }
    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateItem(@PathVariable Long id, @RequestBody MenuItem updated) {
        return menuItemRepository.findById(id).map(item -> {
            item.setName(updated.getName()); item.setDescription(updated.getDescription());
            item.setBasePrice(updated.getBasePrice()); item.setAvailable(updated.isAvailable());
            if (updated.getCategory() != null && updated.getCategory().getId() != null) {
                categoryRepository.findById(updated.getCategory().getId()).ifPresent(item::setCategory);
            }
            if (updated.getImageUrl() != null) item.setImageUrl(updated.getImageUrl());
            if (updated.getCalories() != null) item.setCalories(updated.getCalories());
            item.setSortOrder(updated.getSortOrder());
            item.setPopular(updated.isPopular()); item.setFeatured(updated.isFeatured());
            return ResponseEntity.ok(menuItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<Map<String,Object>> toggleAvailability(@PathVariable Long id) {
        return menuItemRepository.findById(id).map(item -> {
            item.setAvailable(!item.isAvailable());
            menuItemRepository.save(item);
            return ResponseEntity.ok(Map.<String,Object>of("id",item.getId(),"available",item.isAvailable(),"message",item.getName()+" is now "+(item.isAvailable()?"available":"unavailable")));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> deleteItem(@PathVariable Long id) {
        if (!menuItemRepository.existsById(id)) return ResponseEntity.notFound().build();
        menuItemRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message","Menu item deleted successfully"));
    }
}
