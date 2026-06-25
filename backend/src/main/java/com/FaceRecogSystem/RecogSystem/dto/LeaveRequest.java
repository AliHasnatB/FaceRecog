package com.FaceRecogSystem.RecogSystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveRequest {
    @NotNull
    @JsonProperty("profileId")
    private Long studentId;
    @NotNull
    @JsonProperty("yyyy-MMM-dd")
    private LocalDate fromDate;
    @NotNull
    @JsonProperty("yyyy-MMM-dd")
    private LocalDate toDate;
    @NotBlank private String reason;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long id) { this.studentId = id; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate d) { this.fromDate = d; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate d) { this.toDate = d; }
    public String getReason() { return reason; }
    public void setReason(String r) { this.reason = r; }
}
