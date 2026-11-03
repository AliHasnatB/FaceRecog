# FaceOx — Face Recognition Attendance System

A full-stack attendance management system using CompareFace for face recognition,
Spring Boot for the backend, and JavaFX for the desktop frontend.

---

## Tech Stack

| Layer       | Technology                           |
|-------------|--------------------------------------|
| Face API    | CompareFace (Docker)                 |
| Backend     | Spring Boot 3.2, PostgreSQL, JWT     |
| Frontend    | JavaFX 21, Webcam Capture            |
| Auth        | JWT + Face recognition (WebSocket)   |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker (for CompareFace)

---

## 1. Start CompareFace

```bash
docker run -p 8000:80 \
  -e postgres_password=postgres \
  -e postgres_db=facedb \
  exadel/compreface:latest
```

Then open http://localhost:8000, create an application, get the API key,
and set it in `application.properties`.

---

## 2. Configure the Backend

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/face_attendance
spring.datasource.username=postgres
spring.datasource.password=YOUR_DB_PASSWORD

jwt.secret=your-256-bit-secret-key-here-must-be-long-enough
compareface.url=http://localhost:8000
compareface.api-key=YOUR_COMPAREFACE_API_KEY
```

Create the database:
```sql
CREATE DATABASE face_attendance;
```

---

## 3. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at http://localhost:8080

---

## 4. Run the JavaFX Frontend

```bash
cd frontend
mvn javafx:run
```

---

## 5. First-Time Setup (Admin Account)

Insert the admin user directly into the database (password is BCrypt of "admin123"):

```sql
INSERT INTO users (username, password, email, role, active, created_at)
VALUES (
  'admin',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKBV8L6XFfGFDHGOB5OsGMpFONme',  -- admin123
  'admin@school.edu',
  'ADMIN',
  true,
  NOW()
);
```

---

## 6. API Endpoints Reference

### Auth
| Method | URL                    | Body / Notes         |
|--------|------------------------|----------------------|
| POST   | /api/auth/login        | username, password   |

### Students
| Method | URL                              | Role        |
|--------|----------------------------------|-------------|
| POST   | /api/students/register           | ADMIN       |
| GET    | /api/students                    | ADMIN,TEACHER|
| GET    | /api/students/{id}               | ALL         |
| DELETE | /api/students/{id}               | ADMIN       |

### Attendance
| Method | URL                              | Role         |
|--------|----------------------------------|--------------|
| GET    | /api/attendance/today            | ADMIN,TEACHER|
| GET    | /api/attendance/student/{name}   | ALL          |
| GET    | /api/attendance/date/{date}      | ADMIN,TEACHER|
| GET    | /api/attendance/range?start=&end=| ADMIN,TEACHER|
| GET    | /api/attendance/stats/{name}     | ALL          |

### Classes
| Method | URL                                          | Role           |
|--------|----------------------------------------------|----------------|
| POST   | /api/classes                                 | ADMIN,TEACHER  |
| GET    | /api/classes                                 | ALL            |
| GET    | /api/classes/teacher/{teacherId}             | ALL            |
| GET    | /api/classes/department/{deptId}             | ALL            |
| GET    | /api/classes/department/{deptId}/semester/{s}| ALL            |

### Leaves
| Method | URL                           | Role           |
|--------|-------------------------------|----------------|
| POST   | /api/leaves/apply             | STUDENT,ADMIN  |
| GET    | /api/leaves/student/{id}      | ALL            |
| GET    | /api/leaves/pending           | ADMIN,TEACHER  |
| PUT    | /api/leaves/{id}/review       | ADMIN,TEACHER  |

### Fees
| Method | URL                                 | Role    |
|--------|-------------------------------------|---------|
| POST   | /api/fees/create                    | ADMIN   |
| POST   | /api/fees/{id}/pay                  | ADMIN   |
| GET    | /api/fees/student/{id}              | ADMIN,STUDENT|
| GET    | /api/fees/student/{id}/summary      | ADMIN,STUDENT|

### WebSocket
| URL               | Description                              |
|-------------------|------------------------------------------|
| ws://localhost:8080/ws/face | Send JPEG bytes → get JSON with JWT |

---

## 7. Entity Relationship Diagram

```
User ──────────────── Student ──── Department
  │                     │
  │                     ├── Attendance
  │                     ├── Leave
  │                     └── Fee
  │
  └──────────────── Teacher ──── Department
                       │
                       └── SubjectClass ──── Department
```

---

## 8. Role Capabilities

| Feature              | ADMIN | TEACHER | STUDENT |
|----------------------|-------|---------|---------|
| Face login           | ✅    | ✅      | ✅      |
| View attendance      | ✅    | ✅      | Own only|
| Mark attendance (WS) | ✅    | ✅      | ✅      |
| Manage students      | ✅    | View    | Own only|
| Manage teachers      | ✅    | View    | View    |
| Create classes       | ✅    | ✅      | ❌      |
| Approve leaves       | ✅    | ✅      | ❌      |
| Apply for leave      | ❌    | ❌      | ✅      |
| Manage fees          | ✅    | ❌      | View own|
# FaceRecog
# FaceRecog
# FaceRecog
