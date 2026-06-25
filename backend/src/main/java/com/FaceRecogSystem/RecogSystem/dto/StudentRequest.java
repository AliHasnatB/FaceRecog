package com.FaceRecogSystem.RecogSystem.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentRequest {
    @NotBlank private String subjectName;
    @NotBlank private String fullName;
    private String email;
    @NotBlank private String rollNumber;
    private Long departmentId;
    private String semester;
    // User credentials
    @NotBlank private String username;
    @NotBlank private String password;

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { this.subjectName = s; }
    public String getFullName() { return fullName; }
    public void setFullName(String s) { this.fullName = s; }
    public String getEmail() { return email; }
    public void setEmail(String s) { this.email = s; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String s) { this.rollNumber = s; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long id) { this.departmentId = id; }
    public String getSemester() { return semester; }
    public void setSemester(String s) { this.semester = s; }
    public String getUsername() { return username; }
    public void setUsername(String s) { this.username = s; }
    public String getPassword() { return password; }
    public void setPassword(String s) { this.password = s; }
}
