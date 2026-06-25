package com.backex.faceox.model;

import javafx.beans.property.*;

public class FeeRecord {
    private final LongProperty id;
    private final SimpleStringProperty semester, dueDate, status;
    private final SimpleDoubleProperty total, paid, pending;

    public FeeRecord(long id, String semester, double total,
                     double paid, String dueDate, String status) {
        this.id       = new SimpleLongProperty(id);
        this.semester = new SimpleStringProperty(semester);
        this.total    = new SimpleDoubleProperty(total);
        this.paid     = new SimpleDoubleProperty(paid);
        this.pending  = new SimpleDoubleProperty(total - paid);
        this.dueDate  = new SimpleStringProperty(dueDate);
        this.status   = new SimpleStringProperty(status);
    }
    public long getId()           { return id.get(); }
    public LongProperty idProperty() { return id; }
    public String getSemester()   { return semester.get(); }
    public StringProperty semesterProperty() { return semester; }
    public double getTotal()      { return total.get(); }
    public DoubleProperty totalProperty() { return total; }
    public double getPaid()       { return paid.get(); }
    public DoubleProperty paidProperty() { return paid; }
    public double getPending()    { return pending.get(); }
    public DoubleProperty pendingProperty() { return pending; }
    public String getDueDate()    { return dueDate.get(); }
    public StringProperty dueDateProperty() { return dueDate; }
    public String getStatus()     { return status.get(); }
    public StringProperty statusProperty() { return status; }
}
