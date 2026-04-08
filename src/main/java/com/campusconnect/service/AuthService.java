package com.campusconnect.service;

import com.campusconnect.dto.AuthRequest;
import com.campusconnect.dto.AuthResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.dto.UserDTO;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        // Block admin email from being registered via API
        if ("admin@campus.edu".equalsIgnoreCase(request.getEmail().trim())) {
            throw new RuntimeException("This email is reserved. Admin account cannot be registered.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.STUDENT);
        user.setDepartment(request.getDepartment());
        user.setYear(request.getYear());
        user.setJoinedAt(LocalDate.now());
        user.setAvatar(getAvatarFromName(request.getName()));
        userRepository.save(user);
        
        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            System.err.println("Could not send email: " + e.getMessage());
        }

        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        return new AuthResponse(jwtToken, mapToDTO(user));
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));

        // Send premium login notification email (only for non-admin users)
        if (user.getRole() != User.Role.ADMIN) {
            try {
                emailService.sendLoginNotificationEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                System.err.println("Could not send login notification email: " + e.getMessage());
            }
        }

        return new AuthResponse(jwtToken, mapToDTO(user));
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        dto.setYear(user.getYear());
        dto.setAvatar(user.getAvatar());
        dto.setJoinedAt(user.getJoinedAt());
        return dto;
    }

    private String getAvatarFromName(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        String avatar = parts[0].substring(0, 1).toUpperCase();
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            avatar += parts[parts.length - 1].substring(0, 1).toUpperCase();
        }
        return avatar;
    }
}
