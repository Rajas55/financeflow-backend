package org.example.financeflowbackend.controller;

import org.example.financeflowbackend.model.User;
import org.example.financeflowbackend.repository.UserRepository;
import org.example.financeflowbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

//    @PostMapping("/signup")
//    public ResponseEntity<?> signup(@RequestBody Map<String, String> signupRequest) {
//        String email = signupRequest.get("email");
//        String password = signupRequest.get("password");
//        String name = signupRequest.get("name");  // Expect name from the request
//
//        // Check if user exists
//        if (userRepository.findByEmail(email) != null) {
//            return ResponseEntity.badRequest().body("User already exists");
//        }
//
//        // Create and save new user
//        User newUser = new User();
//        newUser.setEmail(email);
//        newUser.setPassword(password);  // Hash this in a real application
//        newUser.setName(name);
//        userRepository.save(newUser);
//
//        // Generate token
//        String token = jwtUtil.generateToken(email);
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "User created successfully");
//        response.put("token", token);
//        response.put("email", email);
//        response.put("name", name);
//        return ResponseEntity.ok(response);
//    }
@PostMapping("/signup")
public ResponseEntity<?> signup(@RequestBody Map<String, String> signupRequest) {
    String email = signupRequest.get("email");
    String password = signupRequest.get("password");
    String name = signupRequest.get("name"); // Expect the name from the request

    // Check if user exists
    if (userRepository.findByEmail(email) != null) {
        return ResponseEntity.badRequest().body("User already exists");
    }

    // Create and save new user
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setPassword(password); // Remember: hash in production
    newUser.setName(name);
    userRepository.save(newUser);

    // Generate token
    String token = jwtUtil.generateToken(email);
    Map<String, String> response = new HashMap<>();
    response.put("message", "User created successfully");
    response.put("token", token);
    response.put("email", email);
    response.put("name", name);
    return ResponseEntity.ok(response);
}
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        // Validate credentials
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate token
        String token = jwtUtil.generateToken(email);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("name", user.getName());
        return ResponseEntity.ok(response);
    }

    // New endpoint to fetch user details based on token
    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        String userEmail = jwtUtil.validateToken(token);
        if (userEmail == null) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        return ResponseEntity.ok(response);
    }
}
