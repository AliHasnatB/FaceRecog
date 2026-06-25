package com.FaceRecogSystem.RecogSystem.dto;

import jakarta.validation.constraints.NotBlank;

public class TeacherRequest {
    private String subjectName;
    @NotBlank private String fullName;
    private String email;
    @NotBlank private String employeeId;
    private Long departmentId;
    private String designation;
    @NotBlank private String username;
    @NotBlank private String password;

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { this.subjectName = s; }
    public String getFullName() { return fullName; }
    public void setFullName(String s) { this.fullName = s; }
    public String getEmail() { return email; }
    public void setEmail(String s) { this.email = s; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String s) { this.employeeId = s; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long id) { this.departmentId = id; }
    public String getDesignation() { return designation; }
    public void setDesignation(String s) { this.designation = s; }
    public String getUsername() { return username; }
    public void setUsername(String s) { this.username = s; }
    public String getPassword() { return password; }
    public void setPassword(String s) { this.password = s; }
}
