# Face Recognition Attendance System — Full Project Structure

## Spring Boot Backend: `RecogSystem/`
```
RecogSystem/
├── pom.xml
└── src/main/java/com/FaceRecogSystem/RecogSystem/
    ├── RecogSystemApplication.java
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── WebSocketConfig.java
    │   └── CorsConfig.java
    ├── entity/
    │   ├── User.java           (role-based auth: ADMIN, TEACHER, STUDENT)
    │   ├── Student.java
    │   ├── Teacher.java
    │   ├── Department.java
    │   ├── SubjectClass.java   (class schedule)
    │   ├── Attendance.java
    │   ├── Leave.java
    │   └── Fee.java
    ├── dto/
    │   ├── StudentRequest.java
    │   ├── TeacherRequest.java
    │   ├── LeaveRequest.java
    │   ├── FeeRequest.java
    │   ├── ClassScheduleRequest.java
    │   ├── LoginRequest.java
    │   ├── AuthResponse.java
    │   └── RecognitionResponse.java
    ├── repository/
    │   ├── UserRepository.java
    │   ├── StudentRepository.java
    │   ├── TeacherRepository.java
    │   ├── DepartmentRepository.java
    │   ├── SubjectClassRepository.java
    │   ├── AttendanceRepository.java
    │   ├── LeaveRepository.java
    │   └── FeeRepository.java
    ├── service/
    │   ├── AuthService.java
    │   ├── StudentService.java
    │   ├── TeacherService.java
    │   ├── AttendanceService.java
    │   ├── LeaveService.java
    │   ├── FeeService.java
    │   ├── DepartmentService.java
    │   ├── SubjectClassService.java
    │   └── CompareFaceService.java
    ├── controller/
    │   ├── AuthController.java
    │   ├── StudentController.java
    │   ├── TeacherController.java
    │   ├── AttendanceController.java
    │   ├── LeaveController.java
    │   ├── FeeController.java
    │   ├── DepartmentController.java
    │   └── SubjectClassController.java
    ├── security/
    │   ├── JwtUtil.java
    │   ├── JwtFilter.java
    │   └── UserDetailsServiceImpl.java
    └── websocket/
        └── FaceWebSocketHandler.java

## JavaFX Frontend: `FaceOx/`
```
FaceOx/
├── pom.xml
└── src/main/
    ├── java/com/backex/faceox/
    │   ├── MainApp.java
    │   ├── controller/
    │   │   ├── LoginController.java       (face + password login)
    │   │   ├── DashboardController.java   (main shell with sidebar)
    │   │   ├── AttendanceController.java
    │   │   ├── StudentController.java
    │   │   ├── TeacherController.java
    │   │   ├── ClassScheduleController.java
    │   │   ├── LeaveController.java
    │   │   └── FeeController.java
    │   ├── service/
    │   │   ├── ApiService.java
    │   │   ├── FaceWebSocketService.java
    │   │   └── SessionManager.java        (holds JWT + role)
    │   └── model/
    │       ├── AttendanceRecord.java
    │       ├── StudentRecord.java
    │       ├── TeacherRecord.java
    │       ├── ClassRecord.java
    │       ├── LeaveRecord.java
    │       └── FeeRecord.java
    └── resources/
        ├── fxml/
        │   ├── login.fxml
        │   ├── dashboard.fxml
        │   ├── attendance.fxml
        │   ├── students.fxml
        │   ├── teachers.fxml
        │   ├── classes.fxml
        │   ├── leaves.fxml
        │   └── fees.fxml
        └── styles/
            └── main.css
```
