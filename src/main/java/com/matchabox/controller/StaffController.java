package com.matchabox.controller;
import com.matchabox.model.Staff;
import com.matchabox.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {
    @Autowired private StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<List<Staff>> getAll() {
        return ResponseEntity.ok(staffRepository.findByIsActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getById(@PathVariable Long id) {
        return staffRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String email = String.valueOf(body.getOrDefault("email", "")).trim();
        if (email.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        if (staffRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        String pin = String.valueOf(body.getOrDefault("pin", "")).trim();
        String phone = String.valueOf(body.getOrDefault("phone", "")).trim();
        String roleText = String.valueOf(body.getOrDefault("role", "BARISTA")).toUpperCase();

        if (name.isBlank() || !pin.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name and a 4-digit PIN are required"));
        }

        Staff staff = new Staff();
        staff.setName(name);
        staff.setEmail(email);
        staff.setPhone(phone);
        staff.setPin(pin);
        staff.setRole(Staff.Role.valueOf(roleText));
        staff.setActive(true);
        staff.setPasswordHash(passwordEncoder.encode("MatchaBox1234"));

        return ResponseEntity.status(HttpStatus.CREATED).body(staffRepository.save(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> update(@PathVariable Long id, @RequestBody Staff updated) {
        return staffRepository.findById(id).map(s -> {
            s.setName(updated.getName()); s.setEmail(updated.getEmail());
            s.setPhone(updated.getPhone()); s.setRole(updated.getRole());
            s.setActive(updated.isActive());
            return ResponseEntity.ok(staffRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> delete(@PathVariable Long id) {
        return staffRepository.findById(id).map(s -> {
            s.setActive(false);
            staffRepository.save(s);
            return ResponseEntity.ok(Map.of("message", "Staff removed"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
