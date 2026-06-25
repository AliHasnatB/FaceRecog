package com.backex.faceox.model;

import javafx.beans.property.*;

public class StudentRecord {
    private final LongProperty id;
    private final SimpleStringProperty fullName, rollNumber, email, department, semester;

    public StudentRecord(long id, String fullName, String rollNumber,
                         String email, String department, String semester) {
        this.id         = new SimpleLongProperty(id);
        this.fullName   = new SimpleStringProperty(fullName);
        this.rollNumber = new SimpleStringProperty(rollNumber);
        this.email      = new SimpleStringProperty(email);
        this.department = new SimpleStringProperty(department);
        this.semester   = new SimpleStringProperty(semester);
    }
    public long getId()            { return id.get(); }
    public LongProperty idProperty() { return id; }
    public String getFullName()    { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    public String getRollNumber()  { return rollNumber.get(); }
    public StringProperty rollNumberProperty() { return rollNumber; }
    public String getEmail()       { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public String getDepartment()  { return department.get(); }
    public StringProperty departmentProperty() { return department; }
    public String getSemester()    { return semester.get(); }
    public StringProperty semesterProperty() { return semester; }
}
