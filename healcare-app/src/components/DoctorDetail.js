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
            setError('Không thể tải thông tin bác sĩ. Vui lòng thử lại sau.');
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
            setSelectedSlot(''); // Reset khung giờ khi đổi ngày
        } catch (ex) {
            console.error(ex);
            setError('Không thể tải lịch trống. Vui lòng thử lại sau.');
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

        try {
            setLoading(true);
            const appointmentDateTime = new Date(`${selectedDate}T${selectedSlot}:00`);
            const appointment = {
                patient: { id: user.id },
                doctor: { id: parseInt(id) },
                appointmentDate: appointmentDateTime.toISOString(), // Định dạng "2025-05-12T09:00:00"
                status: 'PENDING',
                createdAt: new Date().toISOString() // Định dạng "2025-05-11T10:00:00"
            };

            let res = await authApis().post(endpoints['appointments'], appointment);
            setSuccess('Đặt lịch hẹn thành công! Bạn sẽ được chuyển hướng về trang chủ.');
            setTimeout(() => navigate('/'), 2000);
        } catch (ex) {
            console.error(ex);
            setError('Đặt lịch hẹn thất bại. Vui lòng thử lại sau.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDoctor();
    }, [id]);

    useEffect(() => {
        if (doctor) {
            loadAvailableSlots();
        }
    }, [doctor, selectedDate]);

    if (loading && !doctor) {
        return (
            <Container className="my-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tải thông tin bác sĩ...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="my-5">
                <Alert variant="danger">{error}</Alert>
            </Container>
        );
    }

    if (!doctor) {
        return (
            <Container className="my-5">
                <Alert variant="info">Không tìm thấy bác sĩ!</Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            {/* Thông báo */}
            {error && <Alert variant="danger">{error}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            {/* Phần thông tin bác sĩ */}
            <Row className="align-items-center mb-5">
                <Col md={4} className="text-center">
                    <img
                        src={doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                        alt="Doctor Avatar"
                        style={{
                            width: '200px',
                            height: '200px',
                            borderRadius: '50%',
                            objectFit: 'cover',
                            border: '3px solid #007bff',
                        }}
                    />
                </Col>
                <Col md={8}>
                    <h1 className="text-primary" style={{ fontSize: '2rem', fontWeight: 'bold' }}>
                        BS. {doctor.user.firstName} {doctor.user.lastName}
                    </h1>
                    <p style={{ color: '#007bff', fontSize: '1.2rem' }}>
                        {doctor.specialization.name}
                    </p>
                    <p style={{ color: '#666', fontSize: '1rem' }}>
                        <FaHospital className="me-2" /> {doctor.hospital.name}
                    </p>
                    <div className="d-flex align-items-center mb-2">
                        <FaStar className="me-1" style={{ color: '#f1c40f' }} />
                        <span>{doctor.rating} ({doctor.experienceYears} năm kinh nghiệm)</span>
                    </div>
                </Col>
            </Row>

            {/* Phần thông tin chi tiết và lịch trống */}
            <Row>
                <Col md={6}>
                    <h3 style={{ color: '#1a3c34', fontWeight: 'bold' }}>Giới thiệu</h3>
                    <p style={{ color: '#666', lineHeight: '1.6' }}>{doctor.bio || 'Chưa có thông tin giới thiệu.'}</p>
                    <h3 style={{ color: '#1a3c34', fontWeight: 'bold', marginTop: '20px' }}>Kinh nghiệm</h3>
                    <p style={{ color: '#666' }}>{doctor.experienceYears} năm kinh nghiệm trong lĩnh vực {doctor.specialization.name}.</p>
                </Col>
                <Col md={6}>
                    <h3 style={{ color: '#1a3c34', fontWeight: 'bold' }}>Đặt lịch hẹn</h3>
                    <Form.Group className="mb-3">
                        <Form.Label>Chọn ngày</Form.Label>
                        <Form.Control
                            type="date"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                            min={new Date().toISOString().split('T')[0]}
                        />
                    </Form.Group>
                    {slotsLoading ? (
                        <div className="text-center">
                            <Spinner animation="border" variant="primary" />
                            <p>Đang tải lịch trống...</p>
                        </div>
                    ) : availableSlots.length > 0 ? (
                        <>
                            <div className="d-flex flex-wrap mb-3">
                                {availableSlots.map((slot, index) => (
                                    <Button
                                        key={index}
                                        variant={selectedSlot === slot ? 'primary' : 'outline-primary'}
                                        className="m-1 rounded-pill"
                                        style={{ minWidth: '120px' }}
                                        onClick={() => setSelectedSlot(slot)}
                                    >
                                        <FaCalendarAlt className="me-1" /> {slot}
                                    </Button>
                                ))}
                            </div>
                            <div className="text-center">
                                <Button
                                    variant="success"
                                    className="rounded-pill px-4 py-2"
                                    onClick={bookAppointment}
                                    disabled={loading || !selectedSlot}
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
                        <Alert variant="info">Không có khung giờ trống vào ngày này.</Alert>
                    )}
                </Col>
            </Row>
        </Container>
    );
};

export default DoctorDetail;