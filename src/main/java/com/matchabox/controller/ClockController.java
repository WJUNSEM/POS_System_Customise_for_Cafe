package com.matchabox.controller;
import com.matchabox.model.ClockRecord;
import com.matchabox.model.Staff;
import com.matchabox.repository.ClockRecordRepository;
import com.matchabox.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
@RestController
@RequestMapping("/api/clock")
@CrossOrigin(origins = "*")
public class ClockController {
    @Autowired private StaffRepository staffRepository;
    @Autowired private ClockRecordRepository clockRepo;

    @GetMapping("/today")
    public ResponseEntity<List<ClockRecord>> getTodayRecords() {
        return ResponseEntity.ok(clockRepo.findByShiftDate(LocalDate.now()));
    }

    @PostMapping("/in")
    public ResponseEntity<Map<String,Object>> clockIn(@RequestBody Map<String,Object> body) {
        Long staffId = Long.valueOf(body.get("staffId").toString());
        String pin   = body.get("pin").toString();
        Optional<Staff> staffOpt = staffRepository.findById(staffId);
        if (staffOpt.isEmpty()) return ResponseEntity.notFound().build();
        Staff staff = staffOpt.get();
        if (!staff.verifyPin(pin)) return ResponseEntity.badRequest().body(Map.of("error","Invalid PIN"));
        Optional<ClockRecord> existing = clockRepo.findByStaffIdAndShiftDateAndClockOutIsNull(staffId, LocalDate.now());
        if (existing.isPresent()) return ResponseEntity.badRequest().body(Map.of("error", staff.getFirstName() + " is already clocked in"));
        ClockRecord record = new ClockRecord();
        record.setStaff(staff);
        record.setClockIn(LocalDateTime.now());
        record.setShiftDate(LocalDate.now());
        clockRepo.save(record);
        return ResponseEntity.ok(Map.of("message", staff.getFirstName() + " clocked in successfully","staffName", staff.getName(),"clockIn", record.getClockIn().toString()));
    }

    @PostMapping("/out")
    public ResponseEntity<Map<String,Object>> clockOut(@RequestBody Map<String,Object> body) {
        Long staffId = Long.valueOf(body.get("staffId").toString());
        String pin   = body.get("pin").toString();
        Optional<Staff> staffOpt = staffRepository.findById(staffId);
        if (staffOpt.isEmpty()) return ResponseEntity.notFound().build();
        Staff staff = staffOpt.get();
        if (!staff.verifyPin(pin)) return ResponseEntity.badRequest().body(Map.of("error","Invalid PIN"));
        Optional<ClockRecord> recordOpt = clockRepo.findByStaffIdAndShiftDateAndClockOutIsNull(staffId, LocalDate.now());
        if (recordOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", staff.getFirstName() + " is not clocked in"));
        ClockRecord record = recordOpt.get();
        record.setClockOut(LocalDateTime.now());
        long minutes = ChronoUnit.MINUTES.between(record.getClockIn(), record.getClockOut());
        record.setHoursWorked(BigDecimal.valueOf(minutes / 60.0).setScale(2, RoundingMode.HALF_UP));
        clockRepo.save(record);
        return ResponseEntity.ok(Map.of("message", staff.getFirstName() + " clocked out successfully","hoursWorked", record.getHoursWorked().toString(),"staffName", staff.getName()));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<ClockRecord>> getStaffHistory(@PathVariable Long staffId) {
        return ResponseEntity.ok(clockRepo.findByStaffIdOrderByClockInDesc(staffId));
    }
}
