package com.matchabox.repository;
import com.matchabox.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    List<Staff> findByIsActiveTrue();
    List<Staff> findByRole(Staff.Role role);
}
