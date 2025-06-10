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
    public PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy User");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ bệnh nhân mới được tạo hồ sơ sức khỏe");
        }

        if (patientSelfReportRepository.existsByPatientId(currentUser.getId())) {
            throw new RuntimeException("Hồ sơ đã tồn tại, không cần tạo nữa đâu");
        }

        Patient patient = patientService.getPatientById(currentUser.getId());
        patientSelfReport.setPatient(patient);

        return patientSelfReportRepository.addPatientSelfReport(patientSelfReport);
    }
    
    @Override
    public PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy User");
        }

        PatientSelfReport existingReport;

        if (currentUser.getRole().name().equals("PATIENT")) {
            
            existingReport = patientSelfReportRepository.getPatientSelfReportByPatientId(currentUser.getId(), username);

            if (existingReport == null) {
                throw new RuntimeException("Không tìm thấy Hồ Sơ của bệnh nhân");
            }

            if (existingReport.getPatient().getId() != currentUser.getId()) {
                throw new RuntimeException("Bạn chỉ có thể cập nhật hồ sơ của mình");
            }

            patientSelfReport.setId(existingReport.getId());
            patientSelfReport.setPatient(existingReport.getPatient());

        } else if (currentUser.getRole().name().equals("DOCTOR")) {

            if (patientSelfReport.getPatient() == null || patientSelfReport.getPatient().getId() == 0) {
                throw new RuntimeException("Không thấy Id bệnh nhân");
            }

            existingReport = patientSelfReportRepository.getPatientSelfReportByPatientId(
                    patientSelfReport.getPatient().getId(), username);

            if (existingReport == null) {
                throw new RuntimeException("Không thấy hồ sơ sức khỏe bệnh nhân này");
            }

            patientSelfReport.setId(existingReport.getId());
        } else {
            throw new RuntimeException("Chỉ bệnh nhân và bác sĩ có thể cập nhật hồ sơ");
        }

        return patientSelfReportRepository.updatePatientSelfReport(patientSelfReport);
    }


    @Override
    public PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username) {

        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        if (currentUser.getRole().name().equals("PATIENT")) {
            if (currentUser.getId() != patientId) {
                throw new RuntimeException("Bạn chỉ được xem báo cáo tự đánh giá của chính mình");
            }

            return patientSelfReportRepository.getPatientSelfReportByPatientId(patientId, username);
        } else if (currentUser.getRole().name().equals("DOCTOR")) {
            return patientSelfReportRepository.getPatientSelfReportByPatientId(patientId, username);
        } else {
            throw new RuntimeException("Không có quyền truy cập báo cáo tự đánh giá của bệnh nhân");
        }
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

        if (!currentUser.getRole().name().equals("PATIENT")) {
            return false;
        }

        return patientSelfReportRepository.existsByPatientId(currentUser.getId());
    }
    
        @Override
    public PatientSelfReport getPatientSelfReportById(int id) {
        return patientSelfReportRepository.getPatientSelfReportById(id);
    }
}