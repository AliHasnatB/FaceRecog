package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.entity.Fee;
import com.FaceRecogSystem.RecogSystem.entity.Student;
import com.FaceRecogSystem.RecogSystem.repository.FeeRepository;
import com.FaceRecogSystem.RecogSystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeeService {

    @Autowired private FeeRepository feeRepository;
    @Autowired private StudentRepository studentRepository;

    @Transactional
    public Fee createFee(Long studentId, String semester, double totalAmount, LocalDate dueDate) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Fee fee = new Fee();
        fee.setStudent(s);
        fee.setSemester(semester);
        fee.setTotalAmount(totalAmount);
        fee.setPaidAmount(0);
        fee.setDueDate(dueDate);
        fee.setStatus(Fee.FeeStatus.PENDING);
        return feeRepository.save(fee);
    }

    @Transactional
    public Fee recordPayment(Long feeId, double amount, String transactionId) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee record not found"));
        fee.setPaidAmount(fee.getPaidAmount() + amount);
        fee.setTransactionId(transactionId);
        fee.setPaidDate(LocalDate.now());
        if (fee.getPaidAmount() >= fee.getTotalAmount()) fee.setStatus(Fee.FeeStatus.PAID);
        else fee.setStatus(Fee.FeeStatus.PARTIAL);
        return feeRepository.save(fee);
    }

    public List<Fee> getByStudent(Long studentId) {
        return feeRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public Map<String, Double> getStudentFeeSummary(Long studentId) {
        double total = feeRepository.sumTotalByStudentId(studentId);
        double paid  = feeRepository.sumPaidByStudentId(studentId);
        Map<String, Double> map = new HashMap<>();
        map.put("totalFees", total);
        map.put("paidFees", paid);
        map.put("pendingFees", total - paid);
        return map;
    }

    public List<Fee> getAll() { return feeRepository.findAll(); }
}
