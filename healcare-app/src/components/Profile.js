import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Container, Row, Col, Form, Button, Spinner, Alert, Tabs, Tab } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';

const Profile = () => {
    const navigate = useNavigate();
    const { user } = useMyUser() || {};
    // Thông tin bệnh nhân
    const [userInfo, setUserInfo] = useState({});
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [email, setEmail] = useState('');
    const [dateOfBirth, setDateOfBirth] = useState('');
    const [insuranceNumber, setInsuranceNumber] = useState('');
    const [avatar, setAvatar] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState({ type: '', text: '' });
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('profile');
    // Thông tin sức khỏe bệnh nhân
    const [selfReport, setSelfReport] = useState(null);
    const [hasSelfReport, setHasSelfReport] = useState(false);
    const [height, setHeight] = useState('');
    const [weight, setWeight] = useState('');
    const [personalMedicalHistory, setPersonalMedicalHistory] = useState('');
    const [familyMedicalHistory, setFamilyMedicalHistory] = useState('');
    const [pregnancyHistory, setPregnancyHistory] = useState('');
    const [bloodType, setBloodType] = useState('');
    const [medicationAllergies, setMedicationAllergies] = useState('');
    const [currentMedications, setCurrentMedications] = useState('');
    const [currentTreatments, setCurrentTreatments] = useState('');
    //Thông tin bác sĩ
    const [specialization, setSpecialization] = useState('');
    const [hospital, setHospital] = useState('');
    const [licenseNumber, setLicenseNumber] = useState('');
    const [bio, setBio] = useState('');
    const [experienceYears, setExperienceYears] = useState('');

    const isDoctor = user?.role === 'DOCTOR';
    const isPatient = user?.role === 'PATIENT';

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setAvatar(file);
            setAvatarPreview(URL.createObjectURL(file));
        }
    };

    // Đơn giản hóa kiểm tra user
    useEffect(() => {
        if (!user) return;

        console.log("Thông tin người dùng:", user);

        console.log("Role của người dùng:", user.role);
        console.log("isDoctor:", isDoctor, "isPatient:", isPatient);

        fetchUserProfile();
    }, [user]);

    // // Khởi tạo updatedMedicalHistory khi records thay đổi
    // useEffect(() => {
    //     if (healthRecords.length > 0 && selectedRecordIndex < healthRecords.length) {
    //         setUpdatedMedicalHistory(healthRecords[selectedRecordIndex]?.medicalHistory || '');
    //     }
    // }, [healthRecords, selectedRecordIndex]);

    const fetchUserProfile = useCallback(async () => {
        // Đơn giản hóa kiểm tra user
        if (!user) return;

        setLoading(true);
        try {

            let response;

            if (isPatient) {
                response = await authApis().get(endpoints['patientProfile']);
                console.log("API response:", response.data);

                const patient = response.data.patient;

                setUserInfo(patient);

                const selfReportData = response.data.selfReport;
                setSelfReport(selfReportData);

                // Cải thiện việc truy cập dữ liệu
                if (user) {
                    // Nếu API trả về { user: {...}, ... }
                    setFirstName(patient.user.firstName || '');
                    setLastName(patient.user.lastName || '');
                    setPhoneNumber(patient.user.phoneNumber || '');
                    setEmail(patient.user.email || user.username || '');
                    setAvatarPreview(patient.user.avatar || '');
                } else {
                    // Nếu API trả về thông tin user trực tiếp
                    setFirstName(patient.user.firstName || '');
                    setLastName(patient.user.lastName || '');
                    setPhoneNumber(patient.user.phoneNumber || '');
                    setEmail(patient.user.email || patient.user.username || '');
                    setAvatarPreview(patient.user.avatar || '');
                }

                // Xử lý dateOfBirth từ nhiều vị trí có thể có
                const dateOfBirthSource = patient.dateOfBirth ||
                    (patient.patient && patient.patient.dateOfBirth);

                if (dateOfBirthSource) {
                    const date = new Date(dateOfBirthSource);
                    setDateOfBirth(date.toISOString().split('T')[0]);
                }

                // Xử lý insuranceNumber từ nhiều vị trí có thể có
                setInsuranceNumber(
                    patient.insuranceNumber ||
                    (patient.patient && patient.patient.insuranceNumber) ||
                    ''
                );

                if (response.data.selfReport) {
                    const selfReportData = response.data.selfReport;

                    if (selfReportData.exists) {
                        setSelfReport(selfReportData.report);
                        setHasSelfReport(true);

                        // Cập nhật các trường dữ liệu
                        setHeight(selfReportData.report.height || '');
                        setWeight(selfReportData.report.weight || '');
                        setPersonalMedicalHistory(selfReportData.report.personalMedicalHistory || '');
                        setFamilyMedicalHistory(selfReportData.report.familyMedicalHistory || '');
                        setPregnancyHistory(selfReportData.report.pregnancyHistory || '');
                        setBloodType(selfReportData.report.bloodType || '');
                        setMedicationAllergies(selfReportData.report.medicationAllergies || '');
                        setCurrentMedications(selfReportData.report.currentMedications || '');
                        setCurrentTreatments(selfReportData.report.currentTreatments || '');
                    } else {
                        setHasSelfReport(false);
                        setSelfReport(null);

                        // Reset các trường form
                        setHeight('');
                        setWeight('');
                        setPersonalMedicalHistory('');
                        setFamilyMedicalHistory('');
                        setPregnancyHistory('');
                        setBloodType('');
                        setMedicationAllergies('');
                        setCurrentMedications('');
                        setCurrentTreatments('');
                    }
                } else {
                    console.warn("Không có dữ liệu selfReport trong response profile");
                    setHasSelfReport(false);
                    setSelfReport(null);
                }
            } else if (isDoctor) {
                // Fetch doctor profile
                response = await authApis().get(endpoints['doctorProfile']);
                console.log("Doctor profile response:", response.data);

                const doctor = response.data.doctor;
                setUserInfo(doctor);

                // Set doctor-specific fields
                setSpecialization(doctor.specialization || '');
                setHospital(doctor.hospital || '');
                setLicenseNumber(doctor.licenseNumber || '');
                setBio(doctor.bio || '');
                setExperienceYears(doctor.experienceYears || '');

                // Set common user fields from the nested user object
                if (doctor.user) {
                    setFirstName(doctor.user.firstName || '');
                    setLastName(doctor.user.lastName || '');
                    setPhoneNumber(doctor.user.phoneNumber || '');
                    setEmail(doctor.user.email || user.username || '');
                    setAvatarPreview(doctor.user.avatar || '');
                }
            }
        } catch (error) {
            console.error('Error fetching profile:', error);

            if (error.response?.status === 401) {
                setMessage({
                    type: 'warning',
                    text: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.'
                });
                setTimeout(() => navigate('/login'), 2000);
            } else {
                setMessage({
                    type: 'danger',
                    text: error.response?.data?.message || 'Không thể tải thông tin hồ sơ. Vui lòng thử lại sau.'
                });
            }
        } finally {
            setLoading(false);
        }
    }, [user, navigate]);

    const handleProfileUpdate = async (e) => {
        e.preventDefault();

        // Đơn giản hóa kiểm tra user
        if (!user) {
            setMessage({
                type: 'warning',
                text: 'Không có thông tin người dùng. Vui lòng đăng nhập lại.'
            });
            navigate('/login');
            return;
        }

        setLoading(true);

        // FormData không cần userId nữa
        const patientProfile = {
            user: {
                firstName: firstName,
                lastName: lastName,
                phoneNumber: phoneNumber
            },
            dateOfBirth: dateOfBirth,
            insuranceNumber: insuranceNumber
        };
        const doctorProfile = {
            user: {
                firstName: firstName,
                lastName: lastName,
                phoneNumber: phoneNumber
            },
            specialization: specialization,
            hospital: hospital,
            licenseNumber: licenseNumber,
            bio: bio,
            experienceYears: parseInt(experienceYears) || 0
        };

        try {
            let response;
            if (isPatient) {
                response = await authApis().put(endpoints['patientProfile'], patientProfile);
                console.log("Kết quả cập nhật:", response.data);

                setUserInfo(response.data);
            }
            else if (isDoctor) {
                response = await authApis().put(endpoints['doctorProfile'], doctorProfile);
                setUserInfo(response.data);
            }

            setMessage({
                type: 'success',
                text: 'Cập nhật thông tin hồ sơ thành công!'
            });

        } catch (error) {
            console.error('Lỗi cập nhật thông tin hồ sơ:', error);
            setMessage({
                type: 'danger',
                text: error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật hồ sơ.'
            });
        } finally {
            setLoading(false);
        }
    };

    const handleAvatarUpload = async (e) => {
        e.preventDefault();

        if (!avatar) {
            setMessage({
                type: 'warning',
                text: 'Vui lòng chọn ảnh đại diện'
            });
            return;
        }

        // Đơn giản hóa kiểm tra user
        if (!user) {
            setMessage({
                type: 'warning',
                text: 'Không có thông tin người dùng. Vui lòng đăng nhập lại.'
            });
            return;
        }

        // FormData không cần userId nữa
        const formData = new FormData();
        formData.append('avatar', avatar);

        setLoading(true);
        try {
            const response = await authApis().post(endpoints['userAvatar'], formData);

            setMessage({
                type: 'success',
                text: 'Cập nhật ảnh đại diện thành công!'
            });

            setAvatarPreview(response.data.avatarUrl || response.data.avatar);
        } catch (error) {
            console.error('Error uploading avatar:', error);
            setMessage({
                type: 'danger',
                text: error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật ảnh đại diện. Vui lòng thử lại.'
            });
        } finally {
            setLoading(false);
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();

        // Đơn giản hóa kiểm tra user
        if (!user) {
            setMessage({
                type: 'warning',
                text: 'Không có thông tin người dùng. Vui lòng đăng nhập lại.'
            });
            return;
        }

        if (newPassword !== confirmPassword) {
            setMessage({
                type: 'danger',
                text: 'Mật khẩu mới và xác nhận mật khẩu không khớp.'
            });
            return;
        }

        setLoading(true);
        // FormData không cần userId nữa
        const formData = new FormData();
        formData.append('currentPassword', currentPassword);
        formData.append('newPassword', newPassword);

        try {
            await authApis().post(endpoints['changePassword'], formData);

            setMessage({
                type: 'success',
                text: 'Đổi mật khẩu thành công!'
            });

            setCurrentPassword('');
            setNewPassword('');
            setConfirmPassword('');
        } catch (error) {
            console.error('Error changing password:', error);
            setMessage({
                type: 'danger',
                text: error.response?.data?.message || 'Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại.'
            });
        } finally {
            setLoading(false);
        }
    };

    // Thêm vào sau handlePasswordChange và trước phần return
    const handleSelfReportSubmit = async (e) => {
        e.preventDefault();

        if (!user) {
            setMessage({
                type: 'warning',
                text: 'Không có thông tin người dùng. Vui lòng đăng nhập lại.'
            });
            navigate('/login');
            return;
        }

        setLoading(true);
        try {
            const reportData = {
                height: height ? parseFloat(height) : null,
                weight: weight ? parseFloat(weight) : null,
                personalMedicalHistory,
                familyMedicalHistory,
                pregnancyHistory,
                bloodType,
                medicationAllergies,
                currentMedications,
                currentTreatments
            };

            let response;

            // Trong hàm handleSelfReportSubmit
            if (hasSelfReport) {
                // Cập nhật báo cáo hiện có
                if (selfReport && selfReport.id) {
                    reportData.id = selfReport.id;
                }

                console.log("Đang cập nhật báo cáo sức khỏe:", reportData);
                response = await authApis().put(endpoints['updatePatientSelfReport'], reportData);

                setMessage({
                    type: 'success',
                    text: 'Cập nhật thông tin sức khỏe thành công!'
                });
            } else {
                // Tạo báo cáo mới - đảm bảo endpoint này là đúng
                console.log("Đang tạo báo cáo sức khỏe mới:", reportData);
                response = await authApis().post(endpoints['createPatientSelfReport'], reportData);

                setMessage({
                    type: 'success',
                    text: 'Tạo thông tin sức khỏe thành công!'
                });
            }

            console.log("Kết quả từ server:", response.data);

            // Cập nhật state với dữ liệu từ response
            setSelfReport(response.data);
            setHasSelfReport(true);

            // Cập nhật các trường form
            setHeight(response.data.height || '');
            setWeight(response.data.weight || '');
            setPersonalMedicalHistory(response.data.personalMedicalHistory || '');
            setFamilyMedicalHistory(response.data.familyMedicalHistory || '');
            setPregnancyHistory(response.data.pregnancyHistory || '');
            setBloodType(response.data.bloodType || '');
            setMedicationAllergies(response.data.medicationAllergies || '');
            setCurrentMedications(response.data.currentMedications || '');
            setCurrentTreatments(response.data.currentTreatments || '');

        } catch (error) {
            console.error('Error submitting self report:', error);

            // Xử lý thông báo lỗi từ server
            if (error.response) {
                const errorData = error.response.data;
                let errorMessage = 'Có lỗi xảy ra khi gửi thông tin sức khỏe.';

                if (errorData.message) {
                    errorMessage = errorData.message;
                } else if (typeof errorData === 'string') {
                    errorMessage = errorData;
                }

                setMessage({
                    type: 'danger',
                    text: errorMessage
                });
            } else {
                setMessage({
                    type: 'danger',
                    text: 'Có lỗi kết nối đến máy chủ. Vui lòng thử lại sau.'
                });
            }
        } finally {
            setLoading(false);
        }
    };

    // Hàm helper để làm mới thông tin báo cáo sức khỏe
    const fetchSelfReport = async () => {
        try {
            setLoading(true);
            const response = await authApis().get(endpoints['patientProfile']);
            console.log("Profile response with self report:", response.data);

            if (response.data.selfReport && response.data.selfReport.exists) {
                setSelfReport(response.data.selfReport.report);
                setHasSelfReport(true);

                // Cập nhật các trường form
                setHeight(response.data.selfReport.report.height || '');
                setWeight(response.data.selfReport.report.weight || '');
                setPersonalMedicalHistory(response.data.selfReport.report.personalMedicalHistory || '');
                setFamilyMedicalHistory(response.data.selfReport.report.familyMedicalHistory || '');
                setPregnancyHistory(response.data.selfReport.report.pregnancyHistory || '');
            } else {
                // Reset nếu không có báo cáo
                setSelfReport(null);
                setHasSelfReport(false);
                setHeight('');
                setWeight('');
                setPersonalMedicalHistory('');
                setFamilyMedicalHistory('');
                setPregnancyHistory('');
            }
        } catch (error) {
            console.error('Error fetching profile data:', error);
            setMessage({
                type: 'warning',
                text: 'Không thể tải thông tin hồ sơ và báo cáo sức khỏe.'
            });
        } finally {
            setLoading(false);
        }
    };

    // JSX render code remains unchanged...
    return (
        <Container className="mt-4">
            <h2 className="mb-4 text-center text-primary">Hồ sơ cá nhân</h2>

            {message.text && (
                <Alert variant={message.type} dismissible onClose={() => setMessage({ type: '', text: '' })}>
                    {message.text}
                </Alert>
            )}

            {loading && <div className="text-center mb-3"><Spinner animation="border" /></div>}

            <Tabs
                id="profile-tabs"
                activeKey={activeTab}
                onSelect={(k) => setActiveTab(k)}
                className="mb-3"
            >
                <Tab eventKey="profile" title="Thông tin cá nhân">
                    <Form onSubmit={handleProfileUpdate}>
                        <Row className="mb-3">
                            <Col md={6}>
                                <Form.Group className="mb-3" controlId="formFirstName">
                                    <Form.Label>Tên</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={firstName}
                                        onChange={(e) => setFirstName(e.target.value)}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3" controlId="formLastName">
                                    <Form.Label>Họ</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={lastName}
                                        onChange={(e) => setLastName(e.target.value)}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group as={Row} className="mb-3" controlId="formEmail">
                            <Form.Label column sm="2">Email</Form.Label>
                            <Col sm="10">
                                <Form.Control
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    readOnly
                                />
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} className="mb-3" controlId="formPhone">
                            <Form.Label column sm="2">Số điện thoại</Form.Label>
                            <Col sm="10">
                                <Form.Control
                                    type="tel"
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(e.target.value)}
                                />
                            </Col>
                        </Form.Group>

                        {isPatient && (
                            <>
                                <Form.Group as={Row} className="mb-3" controlId="formDateOfBirth">
                                    <Form.Label column sm="2">Ngày sinh</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="date"
                                            value={dateOfBirth}
                                            onChange={(e) => setDateOfBirth(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3" controlId="formInsurance">
                                    <Form.Label column sm="2">Số BHYT</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="text"
                                            value={insuranceNumber}
                                            onChange={(e) => setInsuranceNumber(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>
                            </>
                        )}

                        {isDoctor && (
                            <>
                                <Form.Group as={Row} className="mb-3" controlId="formSpecialization">
                                    <Form.Label column sm="2">Chuyên khoa</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="text"
                                            value={specialization}
                                            onChange={(e) => setSpecialization(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3" controlId="formHospital">
                                    <Form.Label column sm="2">Bệnh viện</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="text"
                                            value={hospital}
                                            onChange={(e) => setHospital(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3" controlId="formLicenseNumber">
                                    <Form.Label column sm="2">Số giấy phép</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="text"
                                            value={licenseNumber}
                                            onChange={(e) => setLicenseNumber(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3" controlId="formExperienceYears">
                                    <Form.Label column sm="2">Năm kinh nghiệm</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            type="number"
                                            value={experienceYears}
                                            onChange={(e) => setExperienceYears(e.target.value)}
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3" controlId="formBio">
                                    <Form.Label column sm="2">Giới thiệu</Form.Label>
                                    <Col sm="10">
                                        <Form.Control
                                            as="textarea"
                                            rows={3}
                                            value={bio}
                                            onChange={(e) => setBio(e.target.value)}
                                            placeholder="Giới thiệu về bản thân và kinh nghiệm chuyên môn của bạn..."
                                        />
                                    </Col>
                                </Form.Group>
                            </>
                        )}

                        <div className="d-grid gap-2">
                            <Button variant="primary" type="submit" disabled={loading}>
                                {loading ? 'Đang cập nhật...' : 'Cập nhật thông tin'}
                            </Button>
                        </div>
                    </Form>
                </Tab>

                <Tab eventKey="avatar" title="Ảnh đại diện">
                    <Row className="mb-4 align-items-center">
                        <Col md={4} className="text-center">
                            <img
                                src={avatarPreview || 'https://via.placeholder.com/150'}
                                alt="Avatar"
                                className="img-thumbnail rounded-circle"
                                style={{ width: "150px", height: "150px", objectFit: "cover" }}
                            />
                        </Col>
                        <Col md={8}>
                            <Form onSubmit={handleAvatarUpload}>
                                <Form.Group controlId="formFile" className="mb-3">
                                    <Form.Label>Chọn ảnh mới</Form.Label>
                                    <Form.Control
                                        type="file"
                                        accept="image/*"
                                        onChange={handleAvatarChange}
                                    />
                                </Form.Group>
                                <Button
                                    variant="secondary"
                                    type="submit"
                                    disabled={!avatar || loading}
                                >
                                    {loading ? 'Đang tải lên...' : 'Cập nhật ảnh đại diện'}
                                </Button>
                            </Form>
                        </Col>
                    </Row>
                </Tab>

                <Tab eventKey="password" title="Đổi mật khẩu">
                    <Form onSubmit={handlePasswordChange}>
                        <Form.Group as={Row} className="mb-3" controlId="formCurrentPassword">
                            <Form.Label column sm="3">Mật khẩu hiện tại</Form.Label>
                            <Col sm="9">
                                <Form.Control
                                    type="password"
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    required
                                />
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} className="mb-3" controlId="formNewPassword">
                            <Form.Label column sm="3">Mật khẩu mới</Form.Label>
                            <Col sm="9">
                                <Form.Control
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                />
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} className="mb-3" controlId="formConfirmPassword">
                            <Form.Label column sm="3">Xác nhận mật khẩu</Form.Label>
                            <Col sm="9">
                                <Form.Control
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                />
                            </Col>
                        </Form.Group>

                        <div className="d-grid gap-2">
                            <Button variant="warning" type="submit" disabled={loading}>
                                {loading ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
                            </Button>
                        </div>
                    </Form>
                </Tab>
                {/* Chỉ hiển thị khi người dùng là bệnh nhân */}
                {isPatient && (
                    <Tab eventKey="selfReport" title="Thông tin sức khỏe">
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <h3>Thông tin sức khỏe cá nhân</h3>
                            <Button
                                variant="outline-primary"
                                size="sm"
                                onClick={fetchUserProfile}
                                disabled={loading}
                            >
                                {loading ? <Spinner size="sm" animation="border" /> : <i className="fas fa-sync"></i>} Làm mới
                            </Button>
                        </div>

                        <Form onSubmit={handleSelfReportSubmit}>
                            <Row className="mb-3">
                                <Col md={6}>
                                    <Form.Group controlId="formHeight">
                                        <Form.Label>Chiều cao (cm)</Form.Label>
                                        <Form.Control
                                            type="number"
                                            value={height}
                                            onChange={(e) => setHeight(e.target.value)}
                                            placeholder="Nhập chiều cao (cm)"
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group controlId="formWeight">
                                        <Form.Label>Cân nặng (kg)</Form.Label>
                                        <Form.Control
                                            type="number"
                                            value={weight}
                                            onChange={(e) => setWeight(e.target.value)}
                                            placeholder="Nhập cân nặng (kg)"
                                        />
                                    </Form.Group>
                                </Col>
                            </Row>

                            <Form.Group className="mb-3" controlId="formPersonalHistory">
                                <Form.Label>Tiền sử bệnh cá nhân</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={personalMedicalHistory}
                                    onChange={(e) => setPersonalMedicalHistory(e.target.value)}
                                    placeholder="Nhập thông tin về bệnh lý, dị ứng, phẫu thuật trong quá khứ..."
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formFamilyHistory">
                                <Form.Label>Tiền sử bệnh gia đình</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={familyMedicalHistory}
                                    onChange={(e) => setFamilyMedicalHistory(e.target.value)}
                                    placeholder="Nhập thông tin về bệnh lý trong gia đình (cha mẹ, anh chị em ruột)..."
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formPregnancyHistory">
                                <Form.Label>Tiểu sử thai sản (nếu có)</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    value={pregnancyHistory}
                                    onChange={(e) => setPregnancyHistory(e.target.value)}
                                    placeholder="Nhập thông tin về các lần mang thai, sinh nở..."
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBloodType">
                                <Form.Label>Nhóm máu</Form.Label>
                                <Form.Control
                                    as="select"
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
                                </Form.Control>
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formMedicationAllergies">
                                <Form.Label>Dị ứng thuốc</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={2}
                                    value={medicationAllergies}
                                    onChange={(e) => setMedicationAllergies(e.target.value)}
                                    placeholder="Liệt kê các loại thuốc bạn bị dị ứng..."
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formCurrentMedications">
                                <Form.Label>Thuốc đang sử dụng</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={2}
                                    value={currentMedications}
                                    onChange={(e) => setCurrentMedications(e.target.value)}
                                    placeholder="Liệt kê các loại thuốc bạn đang sử dụng..."
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formCurrentTreatments">
                                <Form.Label>Bệnh đang điều trị</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={2}
                                    value={currentTreatments}
                                    onChange={(e) => setCurrentTreatments(e.target.value)}
                                    placeholder="Các bệnh lý bạn đang được điều trị..."
                                />
                            </Form.Group>
                            <div className="d-grid gap-2">
                                <Button variant="primary" type="submit" disabled={loading}>
                                    {loading ? (
                                        <>
                                            <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                                            {' '}{hasSelfReport ? 'Đang cập nhật...' : 'Đang tạo mới...'}
                                        </>
                                    ) : (hasSelfReport ? 'Cập nhật thông tin sức khỏe' : 'Tạo mới báo cáo sức khỏe')}
                                </Button>
                            </div>
                        </Form>
                    </Tab>
                )}
            </Tabs>
        </Container>
    );
};
export default Profile;