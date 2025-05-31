package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.pojo.PatientSelfReport;
import com.can.pojo.User;
import com.can.repositories.PatientSelfReportRepository;
import com.can.services.PatientSelfReportService;
import com.can.services.PatientService;
import com.can.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientSelfReportServiceImpl implements PatientSelfReportService {

    @Autowired
    private PatientSelfReportRepository patientSelfReportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @Override
    public PatientSelfReport getPatientSelfReportById(int id) {
        return patientSelfReportRepository.getPatientSelfReportById(id);
    }

    // Bác sĩ dùng để lấy báo cáo tự đánh giá của bệnh nhân theo ID bệnh nhân
    @Override
    public PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username) {
        // Xác thực người dùng
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // Phân quyền dựa trên vai trò
        if (currentUser.getRole().name().equals("PATIENT")) {
            // Bệnh nhân chỉ được xem báo cáo của chính mình
            if (currentUser.getId() != patientId) {
                throw new RuntimeException("Bạn chỉ được xem báo cáo tự đánh giá của chính mình");
            }
            // Bệnh nhân có quyền xem báo cáo của mình
            return patientSelfReportRepository.getPatientSelfReportByPatientId(patientId, username);
        } else if (currentUser.getRole().name().equals("DOCTOR")) {
            // Bác sĩ được phép xem báo cáo của bất kỳ bệnh nhân nào
            return patientSelfReportRepository.getPatientSelfReportByPatientId(patientId, username);
        } else {
            // Các vai trò khác không được phép xem
            throw new RuntimeException("Không có quyền truy cập báo cáo tự đánh giá của bệnh nhân");
        }
    }

    @Override
    public PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can create self reports");
        }

        // Check if patient already has a self report
        if (patientSelfReportRepository.existsByPatientId(currentUser.getId())) {
            throw new RuntimeException("You already have a self report. Please update it instead.");
        }

        // Set patient for the report
        Patient patient = patientService.getPatientById(currentUser.getId());
        patientSelfReport.setPatient(patient);

        return patientSelfReportRepository.addPatientSelfReport(patientSelfReport);
    }

    @Override
    public PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Lấy thông tin báo cáo hiện có
        PatientSelfReport existingReport;

        // Phân quyền dựa trên vai trò người dùng
        if (currentUser.getRole().name().equals("PATIENT")) {
            // Bệnh nhân chỉ có thể cập nhật báo cáo của chính họ
            existingReport = patientSelfReportRepository.getPatientSelfReportByPatientId(currentUser.getId(), username);

            if (existingReport == null) {
                throw new RuntimeException("No self report found for this patient");
            }

            // Đảm bảo bệnh nhân đang cập nhật báo cáo của chính họ
            if (existingReport.getPatient().getId() != currentUser.getId()) {
                throw new RuntimeException("You can only update your own self report");
            }

            // Set ID và patient từ báo cáo hiện có
            patientSelfReport.setId(existingReport.getId());
            patientSelfReport.setPatient(existingReport.getPatient());

        } else if (currentUser.getRole().name().equals("DOCTOR")) {
            // Bác sĩ cần patientId được chỉ định để cập nhật
            if (patientSelfReport.getPatient() == null || patientSelfReport.getPatient().getId() == 0) {
                throw new RuntimeException("Patient ID is required when a doctor updates a self report");
            }

            // Lấy báo cáo hiện tại của bệnh nhân
            existingReport = patientSelfReportRepository.getPatientSelfReportByPatientId(
                    patientSelfReport.getPatient().getId(), username);

            if (existingReport == null) {
                throw new RuntimeException("No self report found for this patient");
            }

            // Giữ lại patient object và id
            patientSelfReport.setId(existingReport.getId());
        } else {
            throw new RuntimeException("Access denied. Only patients and doctors can update self reports");
        }

        return patientSelfReportRepository.updatePatientSelfReport(patientSelfReport);
    }

    @Override
    public boolean hasPatientSelfReport(int patientId) {
        return patientSelfReportRepository.existsByPatientId(patientId);
    }

    @Override
    public boolean hasCurrentPatientSelfReport(String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            return false;
        }

        return patientSelfReportRepository.existsByPatientId(currentUser.getId());
    }
}