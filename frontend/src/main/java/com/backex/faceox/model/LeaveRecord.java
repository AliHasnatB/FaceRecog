package com.backex.faceox.model;

import javafx.beans.property.*;

public class LeaveRecord {
    private final LongProperty id;
    private final SimpleStringProperty studentName, fromDate, toDate, reason, status;

    public LeaveRecord(long id, String studentName, String fromDate,
                       String toDate, String reason, String status) {
        this.id          = new SimpleLongProperty(id);
        this.studentName = new SimpleStringProperty(studentName);
        this.fromDate    = new SimpleStringProperty(fromDate);
        this.toDate      = new SimpleStringProperty(toDate);
        this.reason      = new SimpleStringProperty(reason);
        this.status      = new SimpleStringProperty(status);
    }
    public long getId()             { return id.get(); }
    public LongProperty idProperty()  { return id; }
    public String getStudentName()  { return studentName.get(); }
    public StringProperty studentNameProperty() { return studentName; }
    public String getFromDate()     { return fromDate.get(); }
    public StringProperty fromDateProperty() { return fromDate; }
    public String getToDate()       { return toDate.get(); }
    public StringProperty toDateProperty() { return toDate; }
    public String getReason()       { return reason.get(); }
    public StringProperty reasonProperty() { return reason; }
    public String getStatus()       { return status.get(); }
    public StringProperty statusProperty() { return status; }
}
