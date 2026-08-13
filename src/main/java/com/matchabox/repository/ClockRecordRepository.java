package com.matchabox.repository;
import com.matchabox.model.ClockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface ClockRecordRepository extends JpaRepository<ClockRecord, Long> {
    List<ClockRecord> findByShiftDate(LocalDate date);
    List<ClockRecord> findByShiftDateBetween(LocalDate start, LocalDate end);
    List<ClockRecord> findByStaffIdOrderByClockInDesc(Long staffId);
    Optional<ClockRecord> findByStaffIdAndShiftDateAndClockOutIsNull(Long staffId, LocalDate date);
}
