import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner, Table } from 'react-bootstrap';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { Link, useSearchParams } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import cookie from 'react-cookies';

const Home = () => {
    const [doctors, setDoctors] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [loadingDoctors, setLoadingDoctors] = useState(true);
    const [loadingAppointments, setLoadingAppointments] = useState(true);
    const [loadingCancel, setLoadingCancel] = useState(false);
    const [loadingReschedule, setLoadingReschedule] = useState(false);
    const { user } = useMyUser() || {};
    const [page, setPage] = useState(1);
    const [q] = useSearchParams();
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // State cho modal đổi lịch hẹn
    const [showRescheduleModal, setShowRescheduleModal] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [newDateTime, setNewDateTime] = useState('');

    const loadDoctors = async () => {
        try {
            setLoadingDoctors(true);
            let url = `${endpoints['doctors']}?page=${page}`;

            let hospId = q.get('hospital');
            let specId = q.get('specialization');
            let doctorName = q.get('doctorName');

            if (hospId) url += `&hospital=${hospId}`;
            if (specId) url += `&specialization=${specId}`;
            if (doctorName) url += `&doctorName=${doctorName}`;

            let res = await Apis.get(url);
            if (res.data.length === 0) setPage(0);
            else {
                if (page === 1) setDoctors(res.data);
                else setDoctors([...doctors, ...res.data]);
            }
        } catch (ex) {
            console.error('Load doctors error:', ex);
            setError('Không thể tải danh sách bác sĩ. Vui lòng thử lại sau.');
        } finally {
            setLoadingDoctors(false);
        }
    };

    const loadAppointments = async () => {
        if (!user) return;

        try {
            setLoadingAppointments(true);
            let url = `${endpoints['appointmentsFilter']}?page=${page}`;

            if (user.role === 'PATIENT') {
                url += `&patientId=${user.id}`;
            } else if (user.role === 'DOCTOR') {
                url += `&doctorId=${user.id}`;
            }

            console.log('Fetching appointments from:', url); // Log để kiểm tra URL
            const res = await authApis().get(url);
            console.log('Appointments response:', res.data); // Log để kiểm tra response
            if (res.data.length === 0) setPage(0);
            else {
                if (page === 1) setAppointments(res.data);
                else setAppointments([...appointments, ...res.data]);
            }
        } catch (ex) {
            console.error('Load appointments error:', ex);
            setError(`Không thể tải danh sách lịch hẹn: ${ex.message || ex}`);
        } finally {
            setLoadingAppointments(false);
        }
    };

    useEffect(() => {
        if (page > 0) loadDoctors();
        if (page > 0 && user) loadAppointments();
    }, [page, q, user]);

    useEffect(() => {
        setPage(1);
        setDoctors([]);
        setAppointments([]);
    }, [q]);

    const loadMore = () => {
        if (!loadingDoctors && !loadingAppointments && page > 0) {
            setPage(page + 1);
        }
    };

    const formatDate = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const cancelAppointment = async (appointmentId) => {
        if (!window.confirm('Bạn có chắc chắn muốn hủy lịch hẹn này?')) return;

        try {
            setLoadingCancel(true);
            const token = cookie.load('token');
            console.log('Token:', token);
            console.log('Canceling appointment ID:', appointmentId);
            const url = endpoints['cancelAppointment'](appointmentId);
            console.log('Request URL:', url);
            const response = await authApis().patch(url);
            console.log('Cancel response:', response.data);
            await loadAppointments();
            setSuccess('Hủy lịch hẹn thành công!');
            setTimeout(() => setSuccess(null), 2000);
        } catch (ex) {
            console.error('Cancel error:', ex);
            let errorMessage = 'Hủy lịch hẹn thất bại: ';
            if (ex.response) {
                errorMessage += ex.response.data || ex.response.statusText;
            } else if (ex.request) {
                errorMessage += 'Không nhận được phản hồi từ server (kiểm tra CORS hoặc network)';
            } else {
                errorMessage += ex.message;
            }
            setError(errorMessage);
        } finally {
            setLoadingCancel(false);
        }
    };

    const openRescheduleModal = (appointment) => {
        setSelectedAppointment(appointment);
        setNewDateTime('');
        setShowRescheduleModal(true);
    };

    const closeRescheduleModal = () => {
        setShowRescheduleModal(false);
        setSelectedAppointment(null);
    };

    const rescheduleAppointment = async () => {
        if (!newDateTime) {
            setError('Vui lòng chọn ngày giờ mới.');
            return;
        }

        try {
            setLoadingReschedule(true);
            const token = cookie.load('token');
            console.log('Token:', token);
            console.log('Rescheduling appointment ID:', selectedAppointment.id, 'to:', newDateTime);
            const body = { newDateTime: new Date(newDateTime).toISOString() };
            console.log('Request body:', body);
            const url = endpoints['rescheduleAppointment'](selectedAppointment.id);
            console.log('Request URL:', url);
            const response = await authApis().patch(url, body);
            console.log('Reschedule response:', response.data);
            await loadAppointments();
            setSuccess('Đổi lịch hẹn thành công!');
            setTimeout(() => setSuccess(null), 2000);
            closeRescheduleModal();
        } catch (ex) {
            console.error('Reschedule error:', ex);
            let errorMessage = 'Đổi lịch hẹn thất bại: ';
            if (ex.response) {
                errorMessage += ex.response.data || ex.response.statusText;
            } else if (ex.request) {
                errorMessage += 'Không nhận được phản hồi từ server (kiểm tra CORS hoặc network)';
            } else {
                errorMessage += ex.message;
            }
            setError(errorMessage);
        } finally {
            setLoadingReschedule(false);
        }
    };

    return (
        <>
            {/* Hero Section */}
            <div
                className="text-white py-5 px-4 mb-4"
                style={{
                    background: "linear-gradient(rgba(13,110,253,0.8), rgba(13,110,253,0.8)), url('/images/hero-doctor.jpg') center/cover no-repeat",
                    borderRadius: '12px'
                }}
            >
                <Container>
                    <h1 className="display-5 fw-bold">Tìm bác sĩ phù hợp với bạn</h1>
                    <p className="lead">Chọn theo chuyên khoa, bệnh viện hoặc tên bác sĩ để được hỗ trợ tốt nhất.</p>
                </Container>
            </div>

            <Container>
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

                {loadingDoctors && <div className="text-center my-4"><Spinner animation="border" variant="primary" /></div>}
                {doctors.length === 0 && !loadingDoctors && <Alert variant="info" className="mt-2">Không có bác sĩ nào!</Alert>}

                <Row>
                    {doctors.map(d => (
                        <Col key={d.id} className="mb-4" md={4} lg={3} sm={6}>
                            <Card className="h-100 shadow-sm border-0" style={{ borderRadius: '16px' }}>
                                <Card.Img
                                    variant="top"
                                    src={d.user.avatar || '/images/doctor-placeholder.jpg'}
                                    style={{
                                        width: '100%',
                                        height: '220px',
                                        objectFit: 'cover',
                                        borderTopLeftRadius: '16px',
                                        borderTopRightRadius: '16px'
                                    }}
                                />
                                <Card.Body className="d-flex flex-column justify-content-between">
                                    <div>
                                        <Card.Title className="fs-5 text-primary">{d.user.firstName} {d.user.lastName}</Card.Title>
                                        <Card.Text className="mb-1"><strong>📞</strong> {d.user.phoneNumber}</Card.Text>
                                        <Card.Text className="mb-1"><strong>🏥</strong> {d.hospital.name}</Card.Text>
                                        <Card.Text className="mb-2"><strong>🩺</strong> {d.specialization.name}</Card.Text>
                                    </div>
                                    <div className="mt-auto d-flex justify-content-between">
                                        <Button as={Link} to={`/doctors/${d.id}`} variant="outline-primary" size="sm">Xem chi tiết</Button>
                                        <Button as={Link} to={`/appointments/new?doctorId=${d.id}`} variant="success" size="sm">Đặt lịch</Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>

                {page > 0 && !loadingDoctors && !loadingAppointments && (
                    <div className="text-center mb-4">
                        <Button variant="primary" onClick={loadMore} className="px-4 py-2 rounded-pill shadow-sm">
                            Xem thêm
                        </Button>
                    </div>
                )}
            </Container>

            <Container>
                {!user && (
                    <Alert variant="warning" className="mt-2">
                        Vui lòng đăng nhập để xem danh sách lịch hẹn!
                        <Link to="/login" className="ms-2">Đăng nhập</Link>
                    </Alert>
                )}

                {user && (
                    <>
                        {loadingAppointments && page === 1 && (
                            <div className="text-center my-4">
                                <Spinner animation="border" variant="primary" />
                            </div>
                        )}

                        {appointments.length === 0 && !loadingAppointments && (
                            <Alert variant="info" className="mt-2">
                                Bạn chưa có lịch hẹn nào!
                            </Alert>
                        )}

                        {appointments.length > 0 && (
                            <Table striped bordered hover responsive className="mt-4">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Tên bác sĩ</th>
                                        <th>Tên bệnh nhân</th>
                                        <th>Ngày hẹn</th>
                                        <th>Trạng thái</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {appointments.map((appt, index) => (
                                        <tr key={appt.id}>
                                            <td>{index + 1}</td>
                                            <td>{`${appt.doctor.user.firstName} ${appt.doctor.user.lastName}`}</td>
                                            <td>{`${appt.patient.user.firstName} ${appt.patient.user.lastName}`}</td>
                                            <td>{formatDate(appt.appointmentDate)}</td>
                                            <td>{appt.status}</td>
                                            <td>
                                                <Button
                                                    variant="warning"
                                                    size="sm"
                                                    className="me-2"
                                                    onClick={() => openRescheduleModal(appt)}
                                                    disabled={appt.status !== 'PENDING'}
                                                >
                                                    Đổi lịch hẹn
                                                </Button>
                                                <Button
                                                    variant="danger"
                                                    size="sm"
                                                    onClick={() => cancelAppointment(appt.id)}
                                                    disabled={appt.status !== 'PENDING' || loadingCancel}
                                                >
                                                    {loadingCancel ? (
                                                        <>
                                                            <Spinner animation="border" size="sm" className="me-2" />
                                                            Đang xử lý...
                                                        </>
                                                    ) : (
                                                        'Hủy lịch hẹn'
                                                    )}
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        )}

                        {page > 0 && !loadingAppointments && !loadingDoctors && (
                            <div className="text-center mb-4">
                                <Button
                                    variant="primary"
                                    onClick={loadMore}
                                    className="px-4 py-2 rounded-pill shadow-sm"
                                >
                                    Xem thêm
                                </Button>
                            </div>
                        )}
                    </>
                )}

                {/* Modal đổi lịch hẹn */}
                <Modal show={showRescheduleModal} onHide={closeRescheduleModal}>
                    <Modal.Header closeButton>
                        <Modal.Title>Đổi lịch hẹn</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Form>
                            <Form.Group className="mb-3">
                                <Form.Label>Chọn ngày giờ mới</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    value={newDateTime}
                                    onChange={(e) => setNewDateTime(e.target.value)}
                                    min={new Date().toISOString().slice(0, 16)}
                                />
                            </Form.Group>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeRescheduleModal}>
                            Đóng
                        </Button>
                        <Button
                            variant="primary"
                            onClick={rescheduleAppointment}
                            disabled={loadingReschedule || !newDateTime}
                        >
                            {loadingReschedule ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Đang xử lý...
                                </>
                            ) : (
                                'Lưu thay đổi'
                            )}
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Container>
        </>
    );
};

export default Home;