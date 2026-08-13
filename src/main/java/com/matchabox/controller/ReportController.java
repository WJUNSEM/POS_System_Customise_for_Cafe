package com.matchabox.controller;

import com.matchabox.model.ClockRecord;
import com.matchabox.model.Order;
import com.matchabox.model.Staff;
import com.matchabox.repository.ClockRecordRepository;
import com.matchabox.repository.OrderRepository;
import com.matchabox.repository.StaffRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final OrderRepository orderRepository;
    private final ClockRecordRepository clockRepository;
    private final StaffRepository staffRepository;

    public ReportController(OrderRepository orderRepository,
                            ClockRecordRepository clockRepository,
                            StaffRepository staffRepository) {
        this.orderRepository = orderRepository;
        this.clockRepository = clockRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("daily", salesFor(today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        result.put("monthly", salesFor(today.withDayOfMonth(1).atStartOfDay(),
                today.withDayOfMonth(1).plusMonths(1).atStartOfDay()));
        result.put("yearly", salesFor(today.withDayOfYear(1).atStartOfDay(),
                today.withDayOfYear(1).plusYears(1).atStartOfDay()));
        result.put("workers", workerSummary(today.withDayOfMonth(1), today));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(
            @RequestParam String start,
            @RequestParam String end) {

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start", startDate.toString());
        result.put("end", endDate.toString());
        result.put("sales", salesFor(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()));
        result.put("workers", workerSummary(startDate, endDate));

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam String start,
            @RequestParam String end) {

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);
        List<ClockRecord> records = clockRepository.findByShiftDateBetween(startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("MATCHA BOX REPORT\n");
        csv.append("Period,").append(startDate).append(" to ").append(endDate).append("\n\n");

        csv.append("SALES SUMMARY\n");
        csv.append("Total Orders,Total Sales\n");
        long validOrders = orders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .count();
        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        csv.append(validOrders).append(",").append(totalSales).append("\n\n");

        csv.append("WORKER SUMMARY\n");
        csv.append("Worker,Email,Role,Working Days,Hours Worked,Sales\n");

        for (Staff staff : staffRepository.findAll()) {
            List<ClockRecord> staffRecords = records.stream()
                    .filter(r -> r.getStaff() != null && Objects.equals(r.getStaff().getId(), staff.getId()))
                    .collect(Collectors.toList());

            long workingDays = staffRecords.stream()
                    .map(ClockRecord::getShiftDate)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            BigDecimal hours = staffRecords.stream()
                    .map(ClockRecord::getHoursWorked)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal workerSales = orders.stream()
                    .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                    .filter(o -> o.getStaff() != null && Objects.equals(o.getStaff().getId(), staff.getId()))
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            csv.append(csv(staff.getName())).append(",")
                    .append(csv(staff.getEmail())).append(",")
                    .append(staff.getRole()).append(",")
                    .append(workingDays).append(",")
                    .append(hours).append(",")
                    .append(workerSales).append("\n");
        }

        csv.append("\nDAILY SALES\n");
        csv.append("Date,Orders,Sales\n");

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            Map<String, Object> day = salesFor(
                    cursor.atStartOfDay(),
                    cursor.plusDays(1).atStartOfDay()
            );
            csv.append(cursor).append(",")
                    .append(day.get("orders")).append(",")
                    .append(day.get("sales")).append("\n");
            cursor = cursor.plusDays(1);
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=matchabox_report_" + startDate + "_to_" + endDate + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private Map<String, Object> salesFor(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);

        List<Order> valid = orders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal sales = valid.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orders", valid.size());
        result.put("sales", sales);
        return result;
    }

    private List<Map<String, Object>> workerSummary(LocalDate start, LocalDate end) {
        List<ClockRecord> records = clockRepository.findByShiftDateBetween(start, end);
        List<Order> orders = orderRepository.findByCreatedAtBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        List<Map<String, Object>> result = new ArrayList<>();

        for (Staff staff : staffRepository.findAll()) {
            List<ClockRecord> staffRecords = records.stream()
                    .filter(r -> r.getStaff() != null && Objects.equals(r.getStaff().getId(), staff.getId()))
                    .collect(Collectors.toList());

            long workingDays = staffRecords.stream()
                    .map(ClockRecord::getShiftDate)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            BigDecimal hours = staffRecords.stream()
                    .map(ClockRecord::getHoursWorked)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal workerSales = orders.stream()
                    .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                    .filter(o -> o.getStaff() != null && Objects.equals(o.getStaff().getId(), staff.getId()))
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", staff.getId());
            row.put("name", staff.getName());
            row.put("email", staff.getEmail());
            row.put("role", staff.getRole());
            row.put("workingDays", workingDays);
            row.put("hoursWorked", hours);
            row.put("sales", workerSales);
            result.add(row);
        }

        return result;
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
