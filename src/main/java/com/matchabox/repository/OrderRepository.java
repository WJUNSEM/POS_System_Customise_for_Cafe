package com.matchabox.repository;
import com.matchabox.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatusOrderByCreatedAtAsc(Order.Status status);
    List<Order> findByStatusInOrderByCreatedAtAsc(List<Order.Status> statuses);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = :date")
    List<Order> findByShiftDate(@Param("date") LocalDate date);
}
