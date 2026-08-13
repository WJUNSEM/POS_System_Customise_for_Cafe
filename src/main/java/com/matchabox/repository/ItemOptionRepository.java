package com.matchabox.repository;
import com.matchabox.model.ItemOption;
import com.matchabox.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {
    List<ItemOption> findByMenuItemOrderBySortOrderAsc(MenuItem menuItem);
}
