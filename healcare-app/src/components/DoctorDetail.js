import { useEffect, useState } from 'react';
import { Button, Container, Row, Col, Spinner, Alert, Form } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import { FaStar, FaHospital, FaCalendarAlt } from 'react-icons/fa';

const DoctorDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useMyUser() || {};
    const [doctor, setDoctor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
    const [availableSlots, setAvailableSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState('');
    const [slotsLoading, setSlotsLoading] = useState(false);

    // Lấy thông tin chi tiết bác sĩ
    const loadDoctor = async () => {
        try {
            setLoading(true);
            let res = await Apis.get(`${endpoints['doctors']}/${id}`);
            setDoctor(res.data);
        } catch (ex) {
            console.error(ex);
            setError(ex.response?.data || 'Không thể tải thông tin bác sĩ. Vui lòng thử lại sau.');
        } finally {
            setLoading(false);
        }
    };

    // Lấy lịch trống của bác sĩ
    const loadAvailableSlots = async () => {
        try {
            setSlotsLoading(true);
            let res = await Apis.get(`${endpoints['doctors']}/${id}/available-slots?date=${selectedDate}`);
            setAvailableSlots(res.data);
            setSelectedSlot('');
        } catch (ex) {
            console.error(ex);
            let errorMessage = ex.response?.data || 'Không thể tải khung giờ trống. Vui lòng thử lại sau.';
            if (ex.response?.status === 401) {
                errorMessage = 'Bạn cần đăng nhập để xem khung giờ trống.';
            } else if (ex.response?.status === 403) {
                errorMessage = 'Bạn không có quyền truy cập khung giờ trống.';
            }
            setError(errorMessage);
        } finally {
            setSlotsLoading(false);
        }
    };

    // Đặt lịch hẹn
    const bookAppointment = async () => {
        if (!user) {
            setError('Bạn cần đăng nhập để đặt lịch hẹn.');
            return;
        }
        if (!selectedSlot) {
            setError('Vui lòng chọn một khung giờ.');
            return;
        }

        // Xác nhận trước khi đặt lịch
        if (!window.confirm(`Xác nhận đặt lịch hẹn với BS. ${doctor.user.firstName} ${doctor.user.lastName} vào ${selectedDate} lúc ${selectedSlot}?`)) {
            return;
        }

        try {
            setLoading(true);
            const appointmentDateTime = new Date(`${selectedDate}T${selectedSlot}:00`);
            const appointment = {
                patient: { id: user.id },
                doctor: { id: parseInt(id) },
                appointmentDate: appointmentDateTime.toISOString(),
                status: 'PENDING',
                createdAt: new Date().toISOString()
            };

            let res = await authApis().post(endpoints['bookAppointment'], appointment);
            setSuccess('Đặt lịch hẹn thành công! Bạn sẽ được chuyển hướng về trang chủ.');
            setTimeout(() => {
                setSuccess(null);
                navigate('/');
            }, 3000);
        } catch (ex) {
            console.error(ex);
            let errorMessage = ex.response?.data || 'Đặt lịch hẹn thất bại. Vui lòng thử lại sau.';
            if (ex.response?.status === 401) {
                errorMessage = 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.';
            } else if (ex.response?.status === 403) {
                errorMessage = 'Bạn không có quyền đặt lịch hẹn.';
            } else if (ex.response?.status === 400) {
                errorMessage = ex.response.data; // Hiển thị lỗi từ backend (ví dụ: "Thời gian đặt lịch phải trong tương lai")
            }
            setError(errorMessage);
            setTimeout(() => setError(null), 5000); // Tự xóa lỗi sau 5 giây
        } finally {
            setLoading(false);
        }
    };

    // Xử lý khi chọn slot (kiểm tra đăng nhập)
    const handleSelectSlot = (slot) => {
        if (!user) {
            setError('Bạn cần đăng nhập để chọn khung giờ.');
            setTimeout(() => setError(null), 5000);
            return;
        }
        setSelectedSlot(slot);
    };

    useEffect(() => {
        loadDoctor();
    }, [id]);

    useEffect(() => {
        if (doctor) {
            loadAvailableSlots();
        }
    }, [doctor, selectedDate]);

    // Xử lý trường hợp đang tải
    if (loading && !doctor) {
        return (
            <Container className="my-5 text-center">
                <Spinner
                    animation="border"
                    variant="primary"
                    style={{ width: '3rem', height: '3rem' }}
                />
                <p className="mt-3 fw-semibold" style={{ color: '#0d6efd' }}>
                    Đang tải thông tin bác sĩ...
                </p>
            </Container>
        );
    }

    // Xử lý trường hợp lỗi
    if (error && !doctor) {
        return (
            <Container className="my-5">
                <Alert
                    variant="danger"
                    className="shadow-sm rounded-pill px-4 py-3"
                    onClose={() => setError(null)}
                    dismissible
                >
                    {error}
                </Alert>
            </Container>
        );
    }

    // Xử lý trường hợp không tìm thấy bác sĩ
    if (!doctor) {
        return (
            <Container className="my-5">
                <Alert
                    variant="info"
                    className="shadow-sm rounded-pill px-4 py-3"
                >
                    Không tìm thấy bác sĩ!
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5 py-4">
            {/* Thông báo */}
            {error && (
                <Alert
                    variant="danger"
                    className="shadow-sm rounded-pill px-4 py-3 mb-4"
                    onClose={() => setError(null)}
                    dismissible
                >
                    {error}
                </Alert>
            )}
            {success && (
                <Alert
                    variant="success"
                    className="shadow-sm rounded-pill px-4 py-3 mb-4"
                    onClose={() => setSuccess(null)}
                    dismissible
                >
                    {success}
                </Alert>
            )}

            {/* Phần thông tin bác sĩ */}
            <Row
                className="align-items-center mb-5 shadow-lg p-4"
                style={{
                    borderRadius: '20px',
                    background: 'linear-gradient(to right, #f8f9fa, #e9ecef)',
                    transition: 'box-shadow 0.3s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.15)'}
                onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
            >
                <Col md={4} className="text-center mb-4 mb-md-0">
                    <img
                        src={doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                        alt="Doctor Avatar"
                        style={{
                            width: '220px',
                            height: '220px',
                            borderRadius: '50%',
                            objectFit: 'cover',
                            border: '4px solid #0d6efd',
                            boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
                            transition: 'transform 0.3s'
                        }}
                        onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                        onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                    />
                </Col>
                <Col md={8}>
                    <h1
                        className="text-primary fw-bold"
                        style={{
                            fontSize: '2.2rem',
                            textShadow: '1px 1px 2px rgba(0,0,0,0.1)'
                        }}
                    >
                        BS. {doctor.user.firstName} {doctor.user.lastName}
                    </h1>
                    <p
                        className="fw-semibold"
                        style={{
                            color: '#20c997',
                            fontSize: '1.3rem',
                            marginBottom: '1rem'
                        }}
                    >
                        {doctor.specialization.name}
                    </p>
                    <p style={{ color: '#333', fontSize: '1.1rem' }}>
                        <FaHospital className="me-2 text-primary" /> {doctor.hospital.name}
                    </p>
                    <div className="d-flex align-items-center mb-3">
                        <FaStar className="me-2" style={{ color: '#f1c40f', fontSize: '1.2rem' }} />
                        <span className="fw-semibold" style={{ color: '#333' }}>
                            {doctor.rating || 'Chưa có đánh giá'} ({doctor.experienceYears} năm kinh nghiệm)
                        </span>
                    </div>
                </Col>
            </Row>

            {/* Phần thông tin chi tiết và lịch trống */}
            <Row className="g-4">
                <Col md={6}>
                    <h3
                        className="fw-bold mb-4"
                        style={{
                            color: '#0d6efd',
                            borderBottom: '2px solid #0d6efd',
                            paddingBottom: '0.5rem'
                        }}
                    >
                        Giới Thiệu
                    </h3>
                    <p style={{ color: '#555', lineHeight: '1.7', fontSize: '1rem' }}>
                        {doctor.bio || 'Chưa có thông tin giới thiệu.'}
                    </p>
                    <h3
                        className="fw-bold mt-4 mb-4"
                        style={{
                            color: '#0d6efd',
                            borderBottom: '2px solid #0d6efd',
                            paddingBottom: '0.5rem'
                        }}
                    >
                        Kinh Nghiệm
                    </h3>
                    <p style={{ color: '#555', fontSize: '1rem' }}>
                        {doctor.experienceYears} năm kinh nghiệm trong lĩnh vực {doctor.specialization.name}.
                    </p>
                </Col>
                <Col md={6}>
                    <h3
                        className="fw-bold mb-4"
                        style={{
                            color: '#0d6efd',
                            borderBottom: '2px solid #0d6efd',
                            paddingBottom: '0.75rem'
                        }}
                    >
                        Đặt Lịch Hẹn
                    </h3>
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold">Chọn ngày</Form.Label>
                        <Form.Control
                            type="date"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                            min={new Date().toISOString().split('T')[0]}
                            required
                            className="border-primary rounded-pill"
                            style={{ padding: '0.75rem' }}
                        />
                    </Form.Group>
                    {slotsLoading ? (
                        <div className="text-center my-4">
                            <Spinner
                                animation="border"
                                variant="primary"
                                style={{ width: '3rem', height: '3rem' }}
                            />
                            <p className="mt-3 fw-semibold" style={{ color: '#0d6efd' }}>
                                Đang tải khung giờ trống...
                            </p>
                        </div>
                    ) : availableSlots.length > 0 ? (
                        <>
                            <div className="d-flex flex-wrap mb-4 gap-3">
                                {availableSlots.map((slot, index) => (
                                    <Button
                                        key={index}
                                        variant={selectedSlot === slot ? 'primary' : 'outline-primary'}
                                        className="rounded-pill px-3 py-2"
                                        style={{
                                            minWidth: '130px',
                                            transition: 'background 0.2s, transform 0.2s'
                                        }}
                                        onClick={() => handleSelectSlot(slot)}
                                        onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                                        onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                                    >
                                        <FaCalendarAlt className="me-2" /> {slot}
                                    </Button>
                                ))}
                            </div>
                            <div className="text-center">
                                <Button
                                    variant="success"
                                    className="rounded-pill px-5 py-2 shadow-sm"
                                    onClick={bookAppointment}
                                    disabled={loading || !selectedSlot}
                                    style={{
                                        backgroundColor: '#20c997',
                                        borderColor: '#20c997',
                                        transition: 'transform 0.2s'
                                    }}
                                    onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                                    onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                                >
                                    {loading ? (
                                        <>
                                            <Spinner animation="border" size="sm" className="me-2" />
                                            Đang xử lý...
                                        </>
                                    ) : (
                                        'Đặt lịch'
                                    )}
                                </Button>
                            </div>
                        </>
                    ) : (
                        <Alert
                            variant="info"
                            className="shadow-sm rounded-pill px-4 py-3"
                        >
                            Không có khung giờ trống vào ngày này.
                        </Alert>
                    )}
                </Col>
            </Row>
        </Container>
    );
};

export default DoctorDetail;