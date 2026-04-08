package com.campusconnect.dto;

import com.campusconnect.entity.User;
import java.time.LocalDate;


public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private String department;
    private String year;
    private String avatar;
    private LocalDate joinedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDate getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDate joinedAt) { this.joinedAt = joinedAt; }
}
