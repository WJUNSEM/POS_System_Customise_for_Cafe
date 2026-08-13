package com.matchabox.repository;
import com.matchabox.model.MenuItem;
import com.matchabox.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByIsAvailableTrue();
    List<MenuItem> findByCategoryAndIsAvailableTrue(Category category);
    List<MenuItem> findByIsPopularTrueAndIsAvailableTrue();
    List<MenuItem> findByIsFeaturedTrueAndIsAvailableTrue();
}
