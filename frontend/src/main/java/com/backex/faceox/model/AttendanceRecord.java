package com.backex.faceox.model;

import javafx.beans.property.*;

public class AttendanceRecord {
    private final SimpleStringProperty fullName, subjectName, date, time, status;
    private final SimpleDoubleProperty similarity;

    public AttendanceRecord(String fullName, String subjectName,
                            double similarity, String date, String time, String status) {
        this.fullName    = new SimpleStringProperty(fullName);
        this.subjectName = new SimpleStringProperty(subjectName);
        this.similarity  = new SimpleDoubleProperty(similarity);
        this.date        = new SimpleStringProperty(date);
        this.time        = new SimpleStringProperty(time);
        this.status      = new SimpleStringProperty(status);
    }
    public String getFullName()    { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    public String getSubjectName() { return subjectName.get(); }
    public StringProperty subjectNameProperty() { return subjectName; }
    public double getSimilarity()  { return similarity.get(); }
    public DoubleProperty similarityProperty() { return similarity; }
    public String getDate()        { return date.get(); }
    public StringProperty dateProperty() { return date; }
    public String getTime()        { return time.get(); }
    public StringProperty timeProperty() { return time; }
    public String getStatus()      { return status.get(); }
    public StringProperty statusProperty() { return status; }
}
