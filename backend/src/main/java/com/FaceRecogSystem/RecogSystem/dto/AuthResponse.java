package com.FaceRecogSystem.RecogSystem.dto;

public class AuthResponse {
    private String token;
    private String role;
    private String fullName;
    private Long profileId;   // studentId or teacherId
    private String subjectName;

    public AuthResponse() {}
    public AuthResponse(String token, String role, String fullName, Long profileId, String subjectName) {
        this.token = token; this.role = role; this.fullName = fullName;
        this.profileId = profileId; this.subjectName = subjectName;
    }

    public String getToken() { return token; }
    public void setToken(String t) { this.token = t; }
    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }
    public String getFullName() { return fullName; }
    public void setFullName(String f) { this.fullName = f; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long id) { this.profileId = id; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { this.subjectName = s; }
}
