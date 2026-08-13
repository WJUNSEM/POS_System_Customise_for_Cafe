package com.matchabox.controller;
import com.matchabox.model.Customer;
import com.matchabox.model.Staff;
import com.matchabox.repository.CustomerRepository;
import com.matchabox.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired private CustomerRepository customerRepository;
    @Autowired private StaffRepository staffRepository;

    // Customer login (email + password)
    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> customerLogin(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email==null||password==null)
            return ResponseEntity.badRequest().body(Map.of("error","Email and password required"));
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid email or password"));
        Customer customer = customerOpt.get();
        // NOTE: In production, use BCrypt to verify: BCrypt.checkpw(password, customer.getPasswordHash())
        // For now, plain comparison (update this before going live!)
        if (!password.equals(customer.getPasswordHash()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid email or password"));
        Map<String,Object> response = new HashMap<>();
        response.put("id",            customer.getId());
        response.put("name",          customer.getName());
        response.put("email",         customer.getEmail());
        response.put("loyaltyPoints", customer.getLoyaltyPoints());
        response.put("message",       "Login successful");
        return ResponseEntity.ok(response);
    }

    // Staff login (staffId + PIN)
    @PostMapping("/staff-login")
    public ResponseEntity<Map<String,Object>> staffLogin(@RequestBody Map<String,Object> body) {
        if (body.get("staffId")==null||body.get("pin")==null)
            return ResponseEntity.badRequest().body(Map.of("error","Staff ID and PIN required"));
        Long staffId = Long.valueOf(body.get("staffId").toString());
        String pin   = body.get("pin").toString();
        Optional<Staff> staffOpt = staffRepository.findById(staffId);
        if (staffOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Staff not found"));
        Staff staff = staffOpt.get();
        if (!staff.verifyPin(pin))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid PIN"));
        if (!staff.isActive())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error","Account is inactive"));
        Map<String,Object> response = new HashMap<>();
        response.put("id",      staff.getId());
        response.put("name",    staff.getName());
        response.put("role",    staff.getRole());
        response.put("initials",staff.getInitials());
        response.put("message", "Welcome, "+staff.getFirstName()+"!");
        return ResponseEntity.ok(response);
    }

    // Customer register
    @PostMapping("/register")
    public ResponseEntity<Map<String,Object>> register(@RequestBody Map<String,String> body) {
        String name  = body.get("name");
        String email = body.get("email");
        String pass  = body.get("password");
        if (name==null||email==null||pass==null)
            return ResponseEntity.badRequest().body(Map.of("error","Name, email and password required"));
        if (customerRepository.findByEmail(email).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error","Email already registered"));
        Customer customer = new Customer();
        customer.setName(name); customer.setEmail(email);
        // NOTE: In production, hash the password: BCrypt.hashpw(pass, BCrypt.gensalt())
        customer.setPasswordHash(pass);
        customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Account created successfully","email",email));
    }
}
