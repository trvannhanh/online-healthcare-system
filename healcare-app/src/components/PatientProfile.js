import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Container, Row, Col, Form, Button, Spinner, Alert, Tabs, Tab } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';

const PatientProfile = () => {
    const navigate = useNavigate();
    const { user } = useMyUser() || {};
    const [patientInfo, setPatientInfo] = useState({});
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
        fetchPatientProfile();
    }, [user]);

    const fetchPatientProfile = useCallback(async () => {
        // Đơn giản hóa kiểm tra user
        if (!user) return;

        setLoading(true);
        try {
            // Gọi API mà không cần truyền userId
            const response = await authApis().get(endpoints['patientProfile']);
            console.log("API response:", response.data);

            const patient = response.data;
            setPatientInfo(patient);

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

        try {
            const response = await authApis().put(endpoints['patientProfile'], patientProfile);
            console.log("Kết quả cập nhật:", response.data);

            setMessage({
                type: 'success',
                text: 'Cập nhật thông tin hồ sơ thành công!'
            });

            setPatientInfo(response.data);
        } catch (error) {
            console.error('Error updating profile:', error);
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
            const response = await authApis().post(endpoints['patientAvatar'], formData);

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
            </Tabs>
        </Container>
    );
};
export default PatientProfile;