import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Card, Alert, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';
import { FaUserMd, FaFileMedical, FaSave, FaArrowLeft } from 'react-icons/fa';

const PatientHealthRecord = () => {
    const { patientId } = useParams();
    const navigate = useNavigate();
    const { user } = useMyUser();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [patient, setPatient] = useState(null);
    const [selfReport, setSelfReport] = useState(null);
    const [height, setHeight] = useState('');
    const [weight, setWeight] = useState('');
    const [personalMedicalHistory, setPersonalMedicalHistory] = useState('');
    const [familyMedicalHistory, setFamilyMedicalHistory] = useState('');
    const [pregnancyHistory, setPregnancyHistory] = useState('');
    const [bloodType, setBloodType] = useState('');
    const [medicationAllergies, setMedicationAllergies] = useState('');
    const [currentMedications, setCurrentMedications] = useState('');
    const [currentTreatments, setCurrentTreatments] = useState('');
    const [doctorNotes, setDoctorNotes] = useState('');

    useEffect(() => {
        if (!user || user.role !== 'DOCTOR') {
            setError("Bạn không có quyền truy cập trang này");
            return;
        }

        fetchPatientData();
    }, [patientId, user]);

    const fetchPatientData = async () => {
        try {
            setLoading(true);
            const patientResponse = await authApis().get(`${endpoints['patients']}/${patientId}`);
            setPatient(patientResponse.data);

            const selfReportResponse = await authApis().get(endpoints.patientHealthRecord(patientId)); if (selfReportResponse.data) {
                setSelfReport(selfReportResponse.data);

                setHeight(selfReportResponse.data.height || '');
                setWeight(selfReportResponse.data.weight || '');
                setPersonalMedicalHistory(selfReportResponse.data.personalMedicalHistory || '');
                setFamilyMedicalHistory(selfReportResponse.data.familyMedicalHistory || '');
                setPregnancyHistory(selfReportResponse.data.pregnancyHistory || '');
                setBloodType(selfReportResponse.data.bloodType || '');
                setMedicationAllergies(selfReportResponse.data.medicationAllergies || '');
                setCurrentMedications(selfReportResponse.data.currentMedications || '');
                setCurrentTreatments(selfReportResponse.data.currentTreatments || '');
                setDoctorNotes(selfReportResponse.data.doctorNotes || '');
            } else {
                setError("Bệnh nhân chưa có báo cáo sức khỏe");
            }
        } catch (err) {
            console.error("Error fetching patient data:", err);
            setError(err.response?.data?.message || "Không thể tải thông tin bệnh nhân");
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!user || user.role !== 'DOCTOR') {
            setError("Bạn không có quyền cập nhật hồ sơ sức khỏe");
            return;
        }

        if (!selfReport) {
            setError("Không tìm thấy báo cáo sức khỏe để cập nhật");
            return;
        }

        try {
            setSaving(true);
            setError(null);

            const updatedSelfReport = {
                id: selfReport.id,
                patient: { id: parseInt(patientId) },
                height: parseFloat(height) || null,
                weight: parseFloat(weight) || null,
                personalMedicalHistory,
                familyMedicalHistory,
                pregnancyHistory,
                bloodType,
                medicationAllergies,
                currentMedications,
                currentTreatments,
                doctorNotes
            };

            const response = await authApis().put(endpoints['updatePatientSelfReport'], updatedSelfReport);

            setSelfReport(response.data);
            setSuccess("Cập nhật hồ sơ sức khỏe bệnh nhân thành công!");

            setTimeout(() => setSuccess(null), 3000);
        } catch (err) {
            console.error("Error updating patient health record:", err);
            setError(err.response?.data?.message || "Không thể cập nhật hồ sơ sức khỏe bệnh nhân");
        } finally {
            setSaving(false);
        }
    };

    if (!user || user.role !== 'DOCTOR') {
        return (
            <Container className="my-5">
                <Alert variant="warning">
                    <FaUserMd className="me-2" /> Trang này chỉ dành cho bác sĩ. Vui lòng đăng nhập với tài khoản bác sĩ.
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <div className="d-flex align-items-center justify-content-between mb-4">
                <h2 className="mb-0">
                    <FaFileMedical className="me-2 text-primary" />
                    Hồ sơ sức khỏe bệnh nhân
                </h2>
                <Button
                    variant="outline-secondary"
                    onClick={() => navigate(-1)}
                    className="d-flex align-items-center"
                >
                    <FaArrowLeft className="me-2" /> Quay lại
                </Button>
            </div>

            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible>
                    {error}
                </Alert>
            )}

            {success && (
                <Alert variant="success" onClose={() => setSuccess(null)} dismissible>
                    {success}
                </Alert>
            )}

            {loading ? (
                <div className="text-center my-5">
                    <Spinner animation="border" variant="primary" />
                    <p className="mt-3">Đang tải thông tin...</p>
                </div>
            ) : (
                <>
                    {/* Patient Information Card */}
                    {patient && (
                        <Card className="mb-4 shadow-sm">
                            <Card.Header className="bg-primary text-white">
                                <h5 className="mb-0">Thông tin bệnh nhân</h5>
                            </Card.Header>
                            <Card.Body>
                                <Row>
                                    <Col md={6}>
                                        <p><strong>Họ tên:</strong> {patient.user?.lastName} {patient.user?.firstName}</p>
                                        <p><strong>Email:</strong> {patient.user?.email}</p>
                                    </Col>
                                    <Col md={6}>
                                        <p><strong>Ngày sinh:</strong> {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString('vi-VN') : 'Chưa cập nhật'}</p>
                                        <p><strong>Số bảo hiểm:</strong> {patient.insuranceNumber || 'Chưa cập nhật'}</p>
                                    </Col>
                                </Row>
                            </Card.Body>
                        </Card>
                    )}

                    {selfReport ? (
                        <Card className="shadow-sm">
                            <Card.Header className="bg-primary text-white">
                                <h5 className="mb-0">Báo cáo sức khỏe</h5>
                            </Card.Header>
                            <Card.Body>
                                <Form onSubmit={handleSubmit}>
                                    <Row>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Chiều cao (cm)</Form.Label>
                                                <Form.Control
                                                    type="number"
                                                    value={height}
                                                    onChange={(e) => setHeight(e.target.value)}
                                                    step="0.1"
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Cân nặng (kg)</Form.Label>
                                                <Form.Control
                                                    type="number"
                                                    value={weight}
                                                    onChange={(e) => setWeight(e.target.value)}
                                                    step="0.1"
                                                />
                                            </Form.Group>
                                        </Col>
                                    </Row>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Nhóm máu</Form.Label>
                                        <Form.Select
                                            value={bloodType}
                                            onChange={(e) => setBloodType(e.target.value)}
                                        >
                                            <option value="">-- Chọn nhóm máu --</option>
                                            <option value="A+">A+</option>
                                            <option value="A-">A-</option>
                                            <option value="B+">B+</option>
                                            <option value="B-">B-</option>
                                            <option value="AB+">AB+</option>
                                            <option value="AB-">AB-</option>
                                            <option value="O+">O+</option>
                                            <option value="O-">O-</option>
                                        </Form.Select>
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Tiền sử bệnh cá nhân</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={personalMedicalHistory}
                                            onChange={(e) => setPersonalMedicalHistory(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Tiền sử bệnh gia đình</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={familyMedicalHistory}
                                            onChange={(e) => setFamilyMedicalHistory(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Tiền sử thai sản</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={pregnancyHistory}
                                            onChange={(e) => setPregnancyHistory(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Dị ứng thuốc</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={medicationAllergies}
                                            onChange={(e) => setMedicationAllergies(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Thuốc đang sử dụng</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={currentMedications}
                                            onChange={(e) => setCurrentMedications(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label>Đang điều trị bệnh</Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={currentTreatments}
                                            onChange={(e) => setCurrentTreatments(e.target.value)}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label><strong>Ghi chú của bác sĩ</strong></Form.Label>
                                        <Form.Control
                                            as="textarea"
                                            rows={5}
                                            value={doctorNotes}
                                            onChange={(e) => setDoctorNotes(e.target.value)}
                                            className="border-primary"
                                        />
                                        <Form.Text>Phần này chỉ bác sĩ mới có thể chỉnh sửa và xem</Form.Text>
                                    </Form.Group>

                                    <div className="d-grid">
                                        <Button
                                            type="submit"
                                            variant="primary"
                                            size="lg"
                                            disabled={saving}
                                            className="d-flex align-items-center justify-content-center"
                                        >
                                            {saving ? (
                                                <>
                                                    <Spinner animation="border" size="sm" className="me-2" />
                                                    Đang lưu...
                                                </>
                                            ) : (
                                                <>
                                                    <FaSave className="me-2" />
                                                    Lưu thông tin
                                                </>
                                            )}
                                        </Button>
                                    </div>
                                </Form>
                            </Card.Body>
                        </Card>
                    ) : (
                        <Alert variant="info">
                            Bệnh nhân này chưa có báo cáo sức khỏe. Vui lòng yêu cầu bệnh nhân tạo báo cáo sức khỏe.
                        </Alert>
                    )}
                </>
            )}
        </Container>
    );
};

export default PatientHealthRecord;