package com.backex.faceox.model;

import javafx.beans.property.*;

public class ClassRecord {
    private final LongProperty id;
    private final SimpleStringProperty subjectName, subjectCode, teacherName,
                                       department, dayOfWeek, startTime, endTime,
                                       room, semester;

    public ClassRecord(long id, String subjectName, String subjectCode,
                       String teacherName, String department, String dayOfWeek,
                       String startTime, String endTime, String room, String semester) {
        this.id          = new SimpleLongProperty(id);
        this.subjectName = new SimpleStringProperty(subjectName);
        this.subjectCode = new SimpleStringProperty(subjectCode);
        this.teacherName = new SimpleStringProperty(teacherName);
        this.department  = new SimpleStringProperty(department);
        this.dayOfWeek   = new SimpleStringProperty(dayOfWeek);
        this.startTime   = new SimpleStringProperty(startTime);
        this.endTime     = new SimpleStringProperty(endTime);
        this.room        = new SimpleStringProperty(room);
        this.semester    = new SimpleStringProperty(semester);
    }
    public long getId()             { return id.get(); }
    public String getSubjectName()  { return subjectName.get(); }
    public StringProperty subjectNameProperty() { return subjectName; }
    public String getSubjectCode()  { return subjectCode.get(); }
    public StringProperty subjectCodeProperty() { return subjectCode; }
    public String getTeacherName()  { return teacherName.get(); }
    public StringProperty teacherNameProperty() { return teacherName; }
    public String getDepartment()   { return department.get(); }
    public StringProperty departmentProperty() { return department; }
    public String getDayOfWeek()    { return dayOfWeek.get(); }
    public StringProperty dayOfWeekProperty() { return dayOfWeek; }
    public String getStartTime()    { return startTime.get(); }
    public StringProperty startTimeProperty() { return startTime; }
    public String getEndTime()      { return endTime.get(); }
    public StringProperty endTimeProperty() { return endTime; }
    public String getRoom()         { return room.get(); }
    public StringProperty roomProperty() { return room; }
    public String getSemester()     { return semester.get(); }
    public StringProperty semesterProperty() { return semester; }
}
