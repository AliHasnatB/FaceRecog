package com.backex.faceox.service;

/**
 * Singleton session store. Populated after successful login (face or password).
 */
public class SessionManager {

    private static SessionManager instance;

    private String token;
    private String role;       // ADMIN | TEACHER | STUDENT
    private String fullName;
    private Long   profileId;  // studentId or teacherId
    private String subjectName;

    private SessionManager() {}

    public static SessionManager get() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setSession(String token, String role, String fullName,
                           Long profileId, String subjectName) {
        this.token       = token;
        this.role        = role;
        this.fullName    = fullName;
        this.profileId   = profileId;
        this.subjectName = subjectName;
    }

    public void clear() {
        token = null; role = null; fullName = null;
        profileId = null; subjectName = null;
    }

    public boolean isLoggedIn()   { return token != null; }
    public String  getToken()     { return token; }
    public String  getRole()      { return role; }
    public String  getFullName()  { return fullName; }
    public Long    getProfileId() { return profileId; }
    public String  getSubjectName() { return subjectName; }

    public boolean isAdmin()   { return "ADMIN".equals(role); }
    public boolean isTeacher() { return "TEACHER".equals(role); }
    public boolean isStudent() { return "STUDENT".equals(role); }
}
