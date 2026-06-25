package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.ClassScheduleRequest;
import com.FaceRecogSystem.RecogSystem.entity.*;
import com.FaceRecogSystem.RecogSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectClassService {

    @Autowired private SubjectClassRepository classRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private DepartmentRepository departmentRepository;

    @Transactional
    public SubjectClass createClass(ClassScheduleRequest req) {
        Teacher teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        SubjectClass sc = new SubjectClass();
        sc.setSubjectName(req.getSubjectName());
        sc.setSubjectCode(req.getSubjectCode());
        sc.setTeacher(teacher);
        sc.setDepartment(dept);
        sc.setDayOfWeek(req.getDayOfWeek());
        sc.setStartTime(req.getStartTime());
        sc.setEndTime(req.getEndTime());
        sc.setRoomNumber(req.getRoomNumber());
        sc.setSemester(req.getSemester());
        return classRepository.save(sc);
    }

    public List<SubjectClass> getByTeacher(Long teacherId) {
        return classRepository.findByTeacherId(teacherId);
    }

    public List<SubjectClass> getByDepartment(Long deptId) {
        return classRepository.findByDepartmentId(deptId);
    }

    public List<SubjectClass> getByDepartmentAndSemester(Long deptId, String semester) {
        return classRepository.findByDepartmentIdAndSemester(deptId, semester);
    }

    public List<SubjectClass> getAll() { return classRepository.findAll(); }

    public Optional<SubjectClass> getById(Long id) { return classRepository.findById(id); }

    @Transactional
    public void toggleActive(Long id) {
        classRepository.findById(id).ifPresent(c -> {
            c.setActive(!c.isActive());
            classRepository.save(c);
        });
    }
}
