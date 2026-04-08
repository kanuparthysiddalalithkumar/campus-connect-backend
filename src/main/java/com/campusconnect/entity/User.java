package com.campusconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String department;
    private String year;
    private String avatar;
    private LocalDate joinedAt;

    public enum Role {
        STUDENT, ADMIN
    }

    public User() {}

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public Role getRole() {
        return this.role;
    }

    public Long getId() { return this.id; }
    public String getName() { return this.name; }
    public String getDepartment() { return this.department; }
    public String getYear() { return this.year; }
    public String getAvatar() { return this.avatar; }
    public java.time.LocalDate getJoinedAt() { return this.joinedAt; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setYear(String year) { this.year = year; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setJoinedAt(LocalDate joinedAt) { this.joinedAt = joinedAt; }
    public void setRole(Role role) { this.role = role; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
