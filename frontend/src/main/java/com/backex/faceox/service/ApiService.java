package com.backex.faceox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ApiService {

    private static final String BASE = "http://localhost:8080/api";
    private final ObjectMapper mapper = new ObjectMapper();

    // ─── Auth ────────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> login(String username, String password) {
        return postJson("/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
    }

    // ─── Registration (public — no auth token needed) ─────────────────────────

    /**
     * POST /api/auth/register  (multipart)
     * data = JSON RegisterRequest, faceImage = JPEG byte[]
     */
    public CompletableFuture<JsonNode> registerAccount(
            String fullName, String username, String password,
            String email, String role,
            String rollNumber, String employeeId,
            Long departmentId, String semester, String designation,
            byte[] faceJpegBytes) {

        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpPost post = new HttpPost(BASE + "/auth/register");
                // No auth header — this endpoint is public

                // Build the JSON data part
                String dataJson = String.format(
                        "{\"fullName\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"," +
                        "\"email\":\"%s\",\"role\":\"%s\",\"rollNumber\":\"%s\"," +
                        "\"employeeId\":\"%s\",\"departmentId\":%s," +
                        "\"semester\":\"%s\",\"designation\":\"%s\"}",
                        esc(fullName), esc(username), esc(password),
                        esc(email), esc(role), esc(rollNumber),
                        esc(employeeId),
                        departmentId != null ? departmentId : "null",
                        esc(semester), esc(designation)
                );

                MultipartEntityBuilder b = MultipartEntityBuilder.create();
                b.addTextBody("data", dataJson, ContentType.APPLICATION_JSON);
                b.addBinaryBody("faceImage", faceJpegBytes,
                        ContentType.IMAGE_JPEG, "face.jpg");
                post.setEntity(b.build());

                return c.execute(post, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    /** POST /api/auth/validate-face  — quick face-detection check */
    public CompletableFuture<JsonNode> validateFace(byte[] faceJpegBytes) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpPost post = new HttpPost(BASE + "/auth/validate-face");
                MultipartEntityBuilder b = MultipartEntityBuilder.create();
                b.addBinaryBody("faceImage", faceJpegBytes,
                        ContentType.IMAGE_JPEG, "face.jpg");
                post.setEntity(b.build());
                return c.execute(post, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    /** Escape special characters for inline JSON string values */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ─── Students ────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> registerStudent(
            String subjectName, String fullName, String rollNumber,
            String email, String department, String username, String password,
            File imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpPost post = new HttpPost(BASE + "/students/register");
                auth(post);
                MultipartEntityBuilder b = MultipartEntityBuilder.create();
                b.addTextBody("subjectName", subjectName);
                b.addTextBody("fullName", fullName);
                b.addTextBody("rollNumber", rollNumber);
                b.addTextBody("username", username);
                b.addTextBody("password", password);
                if (email != null && !email.isBlank()) b.addTextBody("email", email);
                if (department != null && !department.isBlank()) b.addTextBody("department", department);
                b.addBinaryBody("faceImage", imageFile, ContentType.IMAGE_JPEG, imageFile.getName());
                post.setEntity(b.build());
                return c.execute(post, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    public CompletableFuture<JsonNode> getAllStudents() { return getJson("/students"); }

    public CompletableFuture<JsonNode> getStudentById(long id) { return getJson("/students/" + id); }

    public CompletableFuture<JsonNode> deleteStudent(long id) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpDelete del = new HttpDelete(BASE + "/students/" + id);
                auth(del);
                return c.execute(del, r -> mapper.readTree("\"" + r.getCode() + "\""));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    // ─── Teachers ────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> getAllTeachers() { return getJson("/teachers"); }

    // ─── Attendance ──────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> getTodayAttendance() { return getJson("/attendance/today"); }

    public CompletableFuture<JsonNode> getStudentAttendance(String subjectName) {
        return getJson("/attendance/student/" + subjectName);
    }

    public CompletableFuture<JsonNode> getAttendanceStats(String subjectName) {
        return getJson("/attendance/stats/" + subjectName);
    }

    public CompletableFuture<JsonNode> getAttendanceRange(String start, String end) {
        return getJson("/attendance/range?start=" + start + "&end=" + end);
    }

    // ─── Leaves ──────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> applyLeave(long studentId, String from, String to, String reason) {
        String body = String.format(
                "{\"studentId\":%d,\"fromDate\":\"%s\",\"toDate\":\"%s\",\"reason\":\"%s\"}",
                studentId, from, to, reason);
        return postJson("/leaves/apply", body);
    }

    public CompletableFuture<JsonNode> getLeavesByStudent(long studentId) {
        return getJson("/leaves/student/" + studentId);
    }

    public CompletableFuture<JsonNode> getPendingLeaves() { return getJson("/leaves/pending"); }

    public CompletableFuture<JsonNode> getAllLeaves() { return getJson("/leaves"); }

    public CompletableFuture<JsonNode> reviewLeave(long leaveId, String status) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpPut put = new HttpPut(BASE + "/leaves/" + leaveId + "/review?status=" + status);
                auth(put);
                return c.execute(put, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    // ─── Fees ────────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> getFeesByStudent(long studentId) {
        return getJson("/fees/student/" + studentId);
    }

    public CompletableFuture<JsonNode> getFeeSummary(long studentId) {
        return getJson("/fees/student/" + studentId + "/summary");
    }

    // ─── Classes ─────────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> getAllClasses() { return getJson("/classes"); }

    public CompletableFuture<JsonNode> getClassesByTeacher(long teacherId) {
        return getJson("/classes/teacher/" + teacherId);
    }

    public CompletableFuture<JsonNode> getClassesByDept(long deptId) {
        return getJson("/classes/department/" + deptId);
    }

    public CompletableFuture<JsonNode> createClass(String json) {
        return postJson("/classes", json);
    }

    // ─── Departments ─────────────────────────────────────────────────────────

    public CompletableFuture<JsonNode> getDepartments() { return getJson("/departments"); }

    public CompletableFuture<JsonNode> getAllFees() { return getJson("/fees"); }

    /** Type alias used by FeesViewController to make the method reference compile. */
    public interface AllFeesFuture {}

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CompletableFuture<JsonNode> getJson(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpGet get = new HttpGet(BASE + path);
                auth(get);
                return c.execute(get, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    private CompletableFuture<JsonNode> postJson(String path, String json) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient c = client()) {
                HttpPost post = new HttpPost(BASE + path);
                auth(post);
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
                return c.execute(post, r -> mapper.readTree(EntityUtils.toString(r.getEntity())));
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });
    }

    private CloseableHttpClient client() { return HttpClients.createDefault(); }

    private void auth(org.apache.hc.core5.http.HttpRequest req) {
        String token = SessionManager.get().getToken();
        if (token != null) req.setHeader("Authorization", "Bearer " + token);
    }
}
