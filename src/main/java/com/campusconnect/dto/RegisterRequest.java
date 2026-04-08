package com.campusconnect.dto;



public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String department;
    private String year;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDepartment() { return department; }
    public String getYear() { return year; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setDepartment(String department) { this.department = department; }
    public void setYear(String year) { this.year = year; }
}
