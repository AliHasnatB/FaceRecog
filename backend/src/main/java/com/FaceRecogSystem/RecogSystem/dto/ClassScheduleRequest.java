package com.FaceRecogSystem.RecogSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class ClassScheduleRequest {
    @NotBlank private String subjectName;
    @NotBlank private String subjectCode;
    @NotNull  private Long teacherId;
    @NotNull  private Long departmentId;
    @NotBlank private String dayOfWeek;
    @NotNull  private LocalTime startTime;
    @NotNull  private LocalTime endTime;
    private String roomNumber;
    private String semester;

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { this.subjectName = s; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String s) { this.subjectCode = s; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long id) { this.teacherId = id; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long id) { this.departmentId = id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String s) { this.dayOfWeek = s; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime t) { this.startTime = t; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime t) { this.endTime = t; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String s) { this.roomNumber = s; }
    public String getSemester() { return semester; }
    public void setSemester(String s) { this.semester = s; }
}
