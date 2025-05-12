import { useEffect, useState } from 'react';
import { Container, Row, Col, Form, Button, Spinner, Alert } from 'react-bootstrap';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import { FaUserMd, FaHospital, FaCalendarAlt } from 'react-icons/fa';

const AppointmentForm = () => {
    const [searchParams] = useSearchParams();
    const doctorId = searchParams.get('doctorId');
    const navigate = useNavigate();
    const { user } = useMyUser() || {};
    const [doctor, setDoctor] = useState(null);
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
    const [availableSlots, setAvailableSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState('');
    const [loading, setLoading] = useState(true);
    const [slotsLoading, setSlotsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // Lấy thông tin bác sĩ
    const loadDoctor = async () => {
        try {
            let res = await Apis.get(`${endpoints['doctors']}/${doctorId}`);
            setDoctor(res.data);
        } catch (ex) {
            console.error(ex);
            setError('Không thể tải thông tin bác sĩ. Vui lòng thử lại sau.');
        }
    };

    // Lấy khung giờ trống
    const loadAvailableSlots = async () => {
        try {
            setSlotsLoading(true);
            let res = await Apis.get(`${endpoints['doctors']}/${doctorId}/available-slots?date=${selectedDate}`);
            setAvailableSlots(res.data);
            setSelectedSlot(''); // Reset khung giờ khi đổi ngày
        } catch (ex) {
            console.error(ex);
            setError('Không thể tải khung giờ trống. Vui lòng thử lại sau.');
        } finally {
            setSlotsLoading(false);
        }
    };

    // Tạo lịch hẹn
    const createAppointment = async (e) => {
        e.preventDefault();
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
                doctor: { id: parseInt(doctorId) },
                appointmentDate: appointmentDateTime.toISOString(),
                status: 'PENDING',
                createdAt: new Date().toISOString()
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
        if (doctorId) {
            loadDoctor();
        }
    }, [doctorId]);

    useEffect(() => {
        if (doctor) {
            loadAvailableSlots();
        }
    }, [doctor, selectedDate]);

    if (!doctorId) {
        return (
            <Container className="my-5">
                <Alert variant="danger">Không tìm thấy thông tin bác sĩ. Vui lòng quay lại trang chủ.</Alert>
            </Container>
        );
    }

    if (loading && !doctor) {
        return (
            <Container className="my-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tải...</p>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h1 className="text-center mb-4" style={{ color: '#1a3c34', fontWeight: 'bold' }}>
                Đặt lịch hẹn
            </h1>

            {error && <Alert variant="danger">{error}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            <Row className="mb-4 align-items-center">
                <Col md={3} className="text-center">
                    <img
                        src={doctor?.user.avatar || '/images/doctor-placeholder.jpg'}
                        alt="Doctor Avatar"
                        style={{
                            width: '120px',
                            height: '120px',
                            borderRadius: '50%',
                            objectFit: 'cover',
                            border: '2px solid #007bff',
                        }}
                    />
                </Col>
                <Col md={9}>
                    <h3 style={{ color: '#1a3c34', fontWeight: 'bold' }}>
                        BS. {doctor?.user.firstName} {doctor?.user.lastName}
                    </h3>
                    <p style={{ color: '#007bff', fontSize: '1.1rem' }}>
                        {doctor?.specialization.name}
                    </p>
                    <p style={{ color: '#666' }}>
                        <FaHospital className="me-2" /> {doctor?.hospital.name}
                    </p>
                </Col>
            </Row>

            <Form onSubmit={createAppointment}>
                <Row>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Chọn ngày</Form.Label>
                            <Form.Control
                                type="date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                                min={new Date().toISOString().split('T')[0]}
                                required
                            />
                        </Form.Group>
                    </Col>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Chọn khung giờ</Form.Label>
                            {slotsLoading ? (
                                <div className="text-center">
                                    <Spinner animation="border" variant="primary" size="sm" />
                                    <span className="ms-2">Đang tải khung giờ...</span>
                                </div>
                            ) : availableSlots.length > 0 ? (
                                <div className="d-flex flex-wrap">
                                    {availableSlots.map((slot, index) => (
                                        <Button
                                            key={index}
                                            variant={selectedSlot === slot ? 'primary' : 'outline-primary'}
                                            className="m-1 rounded-pill"
                                            style={{ minWidth: '100px' }}
                                            onClick={() => setSelectedSlot(slot)}
                                        >
                                            <FaCalendarAlt className="me-1" /> {slot}
                                        </Button>
                                    ))}
                                </div>
                            ) : (
                                <Alert variant="info">Không có khung giờ trống vào ngày này.</Alert>
                            )}
                        </Form.Group>
                    </Col>
                </Row>
                <div className="text-center mt-4">
                    <Button
                        type="submit"
                        variant="success"
                        className="rounded-pill px-4 py-2"
                        disabled={loading || !selectedSlot}
                    >
                        {loading ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Đang xử lý...
                            </>
                        ) : (
                            'Xác nhận đặt lịch'
                        )}
                    </Button>
                </div>
            </Form>
        </Container>
    );
};

export default AppointmentForm;