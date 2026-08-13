package com.matchabox.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "clock_records")
public class ClockRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = false)
    @JsonIgnoreProperties({"clockRecords","orders","passwordHash","pin"})
    private Staff staff;
    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;
    @Column(name = "clock_out")
    private LocalDateTime clockOut;
    @Column(name = "break_start")
    private LocalDateTime breakStart;
    @Column(name = "break_end")
    private LocalDateTime breakEnd;
    @Column(name = "hours_worked", precision = 5, scale = 2)
    private BigDecimal hoursWorked;
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    @Column(name = "notes", length = 255)
    private String notes;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
    public ClockRecord() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff s) { this.staff = s; }
    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime t) { this.clockIn = t; }
    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime t) { this.clockOut = t; }
    public LocalDateTime getBreakStart() { return breakStart; }
    public void setBreakStart(LocalDateTime t) { this.breakStart = t; }
    public LocalDateTime getBreakEnd() { return breakEnd; }
    public void setBreakEnd(LocalDateTime t) { this.breakEnd = t; }
    public BigDecimal getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(BigDecimal h) { this.hoursWorked = h; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate d) { this.shiftDate = d; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
