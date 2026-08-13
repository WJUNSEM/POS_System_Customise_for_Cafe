package com.matchabox.repository;
import com.matchabox.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AddonRepository extends JpaRepository<Addon, Long> {
    List<Addon> findByIsAvailableTrue();
}
