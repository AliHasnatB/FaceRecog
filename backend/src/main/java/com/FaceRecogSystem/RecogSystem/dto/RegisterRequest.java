package com.FaceRecogSystem.RecogSystem.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank private String fullName;
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String email;
    @NotBlank private String rollNumber;   // for students
    private String  employeeId;            // for teachers
    private Long    departmentId;
    private String  semester;
    private String  designation;           // for teachers
    /** "STUDENT" | "TEACHER"  (ADMIN cannot self-register) */
    @NotBlank private String role;

    public String getFullName()     { return fullName; }
    public void setFullName(String s)   { this.fullName = s; }
    public String getUsername()     { return username; }
    public void setUsername(String s)   { this.username = s; }
    public String getPassword()     { return password; }
    public void setPassword(String s)   { this.password = s; }
    public String getEmail()        { return email; }
    public void setEmail(String s)      { this.email = s; }
    public String getRollNumber()   { return rollNumber; }
    public void setRollNumber(String s) { this.rollNumber = s; }
    public String getEmployeeId()   { return employeeId; }
    public void setEmployeeId(String s) { this.employeeId = s; }
    public Long getDepartmentId()   { return departmentId; }
    public void setDepartmentId(Long l) { this.departmentId = l; }
    public String getSemester()     { return semester; }
    public void setSemester(String s)   { this.semester = s; }
    public String getDesignation()  { return designation; }
    public void setDesignation(String s){ this.designation = s; }
    public String getRole()         { return role; }
    public void setRole(String s)       { this.role = s; }
}
