package com.FaceRecogSystem.RecogSystem.dto;

public class RecognitionResponse {
    private boolean authenticated;
    private String subjectName;
    private String fullName;
    private String role;
    private double similarity;
    private String message;
    private String attendanceStatus;
    private String token;     // JWT on face-login success
    private Long profileId;

    public static RecognitionResponse failed(String message) {
        RecognitionResponse r = new RecognitionResponse();
        r.setAuthenticated(false);
        r.setMessage(message);
        r.setAttendanceStatus("FAILED");
        return r;
    }
    public static RecognitionResponse noFace() {
        RecognitionResponse r = new RecognitionResponse();
        r.setAuthenticated(false);
        r.setMessage("No face detected");
        r.setAttendanceStatus("NO_FACE");
        return r;
    }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean a) { this.authenticated = a; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { this.subjectName = s; }
    public String getFullName() { return fullName; }
    public void setFullName(String s) { this.fullName = s; }
    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }
    public double getSimilarity() { return similarity; }
    public void setSimilarity(double s) { this.similarity = s; }
    public String getMessage() { return message; }
    public void setMessage(String m) { this.message = m; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String s) { this.attendanceStatus = s; }
    public String getToken() { return token; }
    public void setToken(String t) { this.token = t; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long id) { this.profileId = id; }
}
