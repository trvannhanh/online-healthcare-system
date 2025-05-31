import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner, Table } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../configs/Apis";
import { Link, useSearchParams } from "react-router-dom";
import { useMyUser } from "../configs/MyContexts";
import cookie from "react-cookies";
import { FaStar, FaStarHalfAlt, FaRegStar } from 'react-icons/fa';

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
    const [newDateTime, setNewDateTime] = useState("");

    //State cho ratings
    const [ratings, setRatings] = useState({});
    const [loadingRatings, setLoadingRatings] = useState(false);
    const doctorRatings = {};


    const loadDoctors = async () => {
        try {
            setLoadingDoctors(true);
            let url = `${endpoints["doctors"]}?page=${page}`;

            let hospId = q.get("hospital");
            let specId = q.get("specialization");
            let doctorName = q.get("doctorName");

            if (hospId) url += `&hospital=${hospId}`;
            if (specId) url += `&specialization=${specId}`;
            if (doctorName) url += `&doctorName=${doctorName}`;

            let res = await Apis.get(url);
            if (res.data.length === 0) setPage(0);
            else {
                if (page === 1) setDoctors(res.data);
                else setDoctors([...doctors, ...res.data]);
                fetchDoctorRatings(res.data);

            }
        } catch (ex) {
            console.error("Load doctors error:", ex);
            setError("Không thể tải danh sách bác sĩ. Vui lòng thử lại sau.");
        } finally {
            setLoadingDoctors(false);
        }
    };

    const loadAppointments = async () => {
        if (!user || (user.role === "DOCTOR" && !user.isVerified)) {
            setAppointments([]);
            setLoadingAppointments(false);
            return;
        }

        try {
            setLoadingAppointments(true);
            let url = `${endpoints["appointmentsFilter"]}?page=${page}`;

            if (user.role === "PATIENT") {
                url += `&patientId=${user.id}`;
            } else if (user.role === "DOCTOR") {
                url += `&doctorId=${user.id}`;
            }

            const res = await authApis().get(url);
            const appointmentsData = res.data;

            const appointmentsWithPayment = await Promise.all(
                appointmentsData.map(async (appt) => {
                    if (appt.status === "COMPLETED") {
                        try {
                            const paymentRes = await Apis.get(`${endpoints["payment"]}/appointment/${appt.id}`);
                            return { ...appt, payment: paymentRes.data };
                        } catch (ex) {
                            if (ex.response?.status === 404) {
                                return { ...appt, payment: null };
                            }
                            throw ex;
                        }
                    }
                    return { ...appt, payment: null };
                })
            );

            if (appointmentsWithPayment.length === 0) setPage(0);
            else {
                if (page === 1) setAppointments(appointmentsWithPayment);
                else setAppointments([...appointments, ...appointmentsWithPayment]);
            }
        } catch (ex) {
            console.error("Load appointments error:", ex);
            if (ex.response?.status === 403) {
                setError("Bạn không có quyền truy cập lịch hẹn. Tài khoản bác sĩ chưa được xác nhận.");
                setAppointments([]);
            } else {
                setError(`Không thể tải danh sách lịch hẹn: ${ex.message || ex}`);
            }
        } finally {
            setLoadingAppointments(false);
        }
    };

    useEffect(() => {
        if (!user) return;

        if (user.role === "DOCTOR" && !user.isVerified) {
            setLoadingDoctors(false);
            setLoadingAppointments(false);
            return;
        }

        if (page > 0) {
            loadDoctors();
            loadAppointments();
        }
    }, [page, q, user?.id, user?.role, user?.isVerified]);

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
        return date.toLocaleString("vi-VN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    const cancelAppointment = async (appointmentId) => {
        if (!window.confirm("Bạn có chắc chắn muốn hủy lịch hẹn này?")) return;

        try {
            setLoadingCancel(true);
            const url = endpoints["cancelAppointment"](appointmentId);
            const response = await authApis().patch(url);
            await loadAppointments();
            setSuccess("Hủy lịch hẹn thành công!");
            setTimeout(() => setSuccess(null), 2000);
        } catch (ex) {
            console.error("Cancel error:", ex);
            let errorMessage = "Hủy lịch hẹn thất bại: ";
            if (ex.response) {
                errorMessage += ex.response.data || ex.response.statusText;
            } else if (ex.request) {
                errorMessage += "Không nhận được phản hồi từ server (kiểm tra CORS hoặc network)";
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
        setNewDateTime("");
        setShowRescheduleModal(true);
    };

    const closeRescheduleModal = () => {
        setShowRescheduleModal(false);
        setSelectedAppointment(null);
    };

    const rescheduleAppointment = async () => {
        if (!newDateTime) {
            setError("Vui lòng chọn ngày giờ mới.");
            return;
        }

        try {
            setLoadingReschedule(true);
            const body = { newDateTime: new Date(newDateTime).toISOString() };
            const url = endpoints["rescheduleAppointment"](selectedAppointment.id);
            const response = await authApis().patch(url, body);
            await loadAppointments();
            setSuccess("Đổi lịch hẹn thành công!");
            setTimeout(() => setSuccess(null), 2000);
            closeRescheduleModal();
        } catch (ex) {
            console.error("Reschedule error:", ex);
            let errorMessage = "Đổi lịch hẹn thất bại: ";
            if (ex.response) {
                errorMessage += ex.response.data || ex.response.statusText;
            } else if (ex.request) {
                errorMessage += "Không nhận được phản hồi từ server (kiểm tra CORS hoặc network)";
            } else {
                errorMessage += ex.message;
            }
            setError(errorMessage);
        } finally {
            setLoadingReschedule(false);
        }
    };

    const renderStars = (rating) => {
        if (!rating && rating !== 0) return null;

        const stars = [];
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;

        // Thêm các sao đầy đủ
        for (let i = 0; i < fullStars; i++) {
            stars.push(<FaStar key={`full-${i}`} className="me-1" style={{ color: '#f1c40f', fontSize: '0.9rem' }} />);
        }

        // Thêm nửa sao nếu có
        if (hasHalfStar) {
            stars.push(<FaStarHalfAlt key="half" className="me-1" style={{ color: '#f1c40f', fontSize: '0.9rem' }} />);
        }

        // Thêm các sao rỗng còn lại
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (let i = 0; i < emptyStars; i++) {
            stars.push(<FaRegStar key={`empty-${i}`} className="me-1" style={{ color: '#f1c40f', fontSize: '0.9rem' }} />);
        }

        return (
            <div className="d-flex align-items-center">
                {stars}
                <span className="ms-1 text-muted" style={{ fontSize: '0.9rem' }}>({rating.toFixed(1)})</span>
            </div>
        );
    };

    // Thêm hàm để fetch ratings cho các bác sĩ
    const fetchDoctorRatings = async (doctorsToFetch) => {
        if (doctorsToFetch.length === 0) return;

        setLoadingRatings(true);
        const newRatings = { ...ratings };

        try {
            // Lấy ratings cho các bác sĩ chưa có rating
            const promises = doctorsToFetch
                .filter(doctor => !doctorRatings[doctor.id])
                .map(doctor =>
                    Apis.get(endpoints['doctorAverageRating'](doctor.id))
                        .then(response => {
                            doctorRatings[doctor.id] = response.data;
                            newRatings[doctor.id] = response.data;
                        })
                        .catch(err => {
                            console.error(`Error fetching rating for doctor ${doctor.id}:`, err);
                            doctorRatings[doctor.id] = 0;
                            newRatings[doctor.id] = 0;
                        })
                );

            // Thêm các ratings đã có sẵn trong cache
            doctorsToFetch.forEach(doctor => {
                if (doctorRatings[doctor.id] !== undefined) {
                    newRatings[doctor.id] = doctorRatings[doctor.id];
                }
            });

            await Promise.all(promises);
            setRatings(newRatings);
        } catch (error) {
            console.error("Error fetching doctor ratings:", error);
        } finally {
            setLoadingRatings(false);
        }
    };

    
    return (
        <>
            {/* Hero Section */}
            <div
                className="text-white py-5 px-4 mb-5 shadow-lg"
                style={{
                    background: "linear-gradient(rgba(13,110,253,0.85), rgba(32,201,151,0.85)), url('/images/hero-doctor.jpg') center/cover no-repeat",
                    borderRadius: "20px",
                    minHeight: "300px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    textAlign: "center"
                }}
            >
                <Container>
                    <h1 className="display-4 fw-bold mb-3" style={{ textShadow: "2px 2px 4px rgba(0,0,0,0.3)" }}>
                        Tìm Bác Sĩ Phù Hợp Với Bạn
                    </h1>
                    <p className="lead mb-4" style={{ fontSize: "1.25rem", fontWeight: "300" }}>
                        Khám phá các bác sĩ theo chuyên khoa, bệnh viện hoặc tên để nhận được sự chăm sóc tốt nhất.
                    </p>
                    <Button
                        as={Link}
                        to="/appointment"
                        variant="success"
                        className="px-5 py-2 rounded-pill shadow-sm"
                        style={{ backgroundColor: "#20c997", borderColor: "#20c997", transition: "transform 0.2s" }}
                        onMouseEnter={(e) => e.target.style.transform = "scale(1.05)"}
                        onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
                    >
                        Đặt Lịch Ngay
                    </Button>
                </Container>
            </div>

            <Container className="py-4">
                {error && (
                    <Alert 
                        variant="danger" 
                        onClose={() => setError(null)} 
                        dismissible 
                        className="shadow-sm rounded-pill px-4 py-3"
                    >
                        {error}
                    </Alert>
                )}
                {success && (
                    <Alert 
                        variant="success" 
                        onClose={() => setSuccess(null)} 
                        dismissible 
                        className="shadow-sm rounded-pill px-4 py-3"
                    >
                        {success}
                    </Alert>
                )}

                {user?.role === "DOCTOR" && !user?.isVerified && (
                    <Alert 
                        variant="warning" 
                        className="mt-3 shadow-sm rounded-pill px-4 py-3"
                    >
                        Giấy phép hành nghề của bạn chưa được xác nhận. Vui lòng chờ quản trị viên xác nhận trước khi sử dụng các chức năng của hệ thống.
                    </Alert>
                )}

                {loadingDoctors && (
                    <div className="text-center my-5">
                        <Spinner 
                            animation="border" 
                            variant="primary" 
                            style={{ width: "3rem", height: "3rem" }}
                        />
                    </div>
                )}
                {doctors.length === 0 && !loadingDoctors && (
                    <Alert 
                        variant="info" 
                        className="mt-3 shadow-sm rounded-pill px-4 py-3"
                    >
                        Không có bác sĩ nào!
                    </Alert>
                )}

                <Row className="g-4">
                    {doctors.map((d) => (
                        <Col key={d.id} className="mb-4" md={4} lg={3} sm={6}>
                            <Card 
                                className="h-100 shadow-lg border-0" 
                                style={{ 
                                    borderRadius: "20px", 
                                    overflow: "hidden",
                                    transition: "transform 0.3s, box-shadow 0.3s"
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-5px)";
                                    e.currentTarget.style.boxShadow = "0 10px 20px rgba(0,0,0,0.15)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "0 4px 8px rgba(0,0,0,0.1)";
                                }}
                            >
                                <Card.Img
                                    variant="top"
                                    src={d.user.avatar || "/images/doctor-placeholder.jpg"}
                                    style={{
                                        width: "100%",
                                        height: "250px",
                                        objectFit: "cover",
                                        borderTopLeftRadius: "20px",
                                        borderTopRightRadius: "20px",
                                    }}
                                />
                                <Card.Body className="d-flex flex-column justify-content-between p-4">
                                    <div>
                                        <Card.Title className="fs-5 fw-bold text-primary mb-3">
                                            {d.user.firstName} {d.user.lastName}
                                        </Card.Title>
                                        <Card.Text className="mb-2" style={{ fontSize: "0.95rem" }}>
                                            <strong>📞</strong> {d.user.phoneNumber}
                                        </Card.Text>
                                        <Card.Text className="mb-2" style={{ fontSize: "0.95rem" }}>
                                            <strong>🏥</strong> {d.hospital.name}
                                        </Card.Text>
                                        <Card.Text className="mb-3" style={{ fontSize: "0.95rem" }}>
                                            <strong>🩺</strong> {d.specialization.name}
                                        </Card.Text>
                                                                                <div className="mb-2">
                                            {ratings[d.id] !== undefined ? renderStars(ratings[d.id]) : <Spinner size="sm" />}
                                        </div>
                                    </div>
                                    <div className="mt-auto d-flex justify-content-between gap-2">
                                        <Button 
                                            as={Link} 
                                            to={`/doctors/${d.id}`} 
                                            variant="outline-primary" 
                                            size="sm"
                                            className="rounded-pill flex-grow-1"
                                            style={{ transition: "background 0.2s" }}
                                        >
                                            Xem Chi Tiết
                                        </Button>
                                        <Button
                                            as={Link}
                                            to={`/doctors/${d.id}`}
                                            variant="success"
                                            size="sm"
                                            className="rounded-pill flex-grow-1"
                                            disabled={user && user.role === "DOCTOR" && !user.isVerified}
                                            style={{ backgroundColor: "#20c997", borderColor: "#20c997" }}
                                        >
                                            Đặt Lịch
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>

                {page > 0 && !loadingDoctors && !loadingAppointments && user?.role !== "DOCTOR" && (
                    <div className="text-center my-5">
                        <Button 
                            variant="primary" 
                            onClick={loadMore} 
                            className="px-5 py-2 rounded-pill shadow-sm"
                            style={{ 
                                backgroundColor: "#0d6efd", 
                                borderColor: "#0d6efd",
                                transition: "transform 0.2s"
                            }}
                            onMouseEnter={(e) => e.target.style.transform = "scale(1.05)"}
                            onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
                        >
                            Xem Thêm
                        </Button>
                    </div>
                )}
            </Container>

            <Container className="py-4">
                {!user && (
                    <Alert 
                        variant="warning" 
                        className="mt-3 shadow-sm rounded-pill px-4 py-3"
                    >
                        Vui lòng đăng nhập để xem danh sách lịch hẹn! 
                        <Link to="/login" className="ms-2 text-decoration-none fw-semibold">
                            Đăng Nhập
                        </Link>
                    </Alert>
                )}

                {user && (
                    <>
                        {loadingAppointments && page === 1 && (
                            <div className="text-center my-5">
                                <Spinner 
                                    animation="border" 
                                    variant="primary" 
                                    style={{ width: "3rem", height: "3rem" }}
                                />
                            </div>
                        )}

                        {appointments.length === 0 && !loadingAppointments && (
                            <Alert 
                                variant="info" 
                                className="mt-3 shadow-sm rounded-pill px-4 py-3"
                            >
                                Bạn chưa có lịch hẹn nào!
                            </Alert>
                        )}

                        {appointments.length > 0 && (
                            <Table 
                                striped 
                                bordered 
                                hover 
                                responsive 
                                className="mt-4 shadow-sm"
                                style={{ borderRadius: "10px", overflow: "hidden" }}
                            >
                                <thead style={{ backgroundColor: "#0d6efd", color: "#fff" }}>
                                    <tr>
                                        <th className="py-3 text-center">#</th>
                                        <th className="py-3">Tên Bác Sĩ</th>
                                        <th className="py-3">Tên Bệnh Nhân</th>
                                        <th className="py-3">Ngày Hẹn</th>
                                        <th className="py-3">Trạng Thái</th>
                                        <th className="py-3">Hành Động</th>
                                        <th className="py-3">Thanh Toán/Chat</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {appointments.map((appt, index) => (
                                        <tr key={appt.id}>
                                            <td className="text-center align-middle">{index + 1}</td>
                                            <td className="align-middle">{`${appt.doctor.user.firstName} ${appt.doctor.user.lastName}`}</td>
                                            <td className="align-middle">{`${appt.patient.user.firstName} ${appt.patient.user.lastName}`}</td>
                                            <td className="align-middle">{formatDate(appt.appointmentDate)}</td>
                                            <td className="align-middle">{appt.status}</td>
                                            <td className="align-middle">
                                                <Button
                                                    variant="warning"
                                                    size="sm"
                                                    className="me-2 rounded-pill px-3"
                                                    onClick={() => openRescheduleModal(appt)}
                                                    disabled={
                                                        appt.status !== "PENDING" ||
                                                        (user && user.role === "DOCTOR" && !user.isVerified)
                                                    }
                                                    style={{ transition: "background 0.2s" }}
                                                >
                                                    Đổi Lịch
                                                </Button>
                                                <Button
                                                    variant="danger"
                                                    size="sm"
                                                    className="rounded-pill px-3"
                                                    onClick={() => cancelAppointment(appt.id)}
                                                    disabled={
                                                        appt.status !== "PENDING" ||
                                                        loadingCancel ||
                                                        (user && user.role === "DOCTOR" && !user.isVerified)
                                                    }
                                                    style={{ transition: "background 0.2s" }}
                                                >
                                                    {loadingCancel ? (
                                                        <>
                                                            <Spinner animation="border" size="sm" className="me-2" />
                                                            Đang Xử Lý...
                                                        </>
                                                    ) : (
                                                        "Hủy Lịch"
                                                    )}
                                                </Button>
                                            </td>
                                            <td className="align-middle">
                                                {user.role === "DOCTOR" &&
                                                    appt.status === "COMPLETED" &&
                                                    !appt.payment && (
                                                        <Button
                                                            as={Link}
                                                            to={`/payment/${appt.id}`}
                                                            variant="primary"
                                                            size="sm"
                                                            className="rounded-pill px-3"
                                                            disabled={!user.isVerified}
                                                            style={{ backgroundColor: "#0d6efd", borderColor: "#0d6efd" }}
                                                        >
                                                            Tạo Hóa Đơn
                                                        </Button>
                                                    )}
                                                {user.role === "PATIENT" &&
                                                    appt.status === "COMPLETED" &&
                                                    appt.payment && (
                                                        <Button
                                                            as={Link}
                                                            to={`/payment/${appt.id}`}
                                                            variant="success"
                                                            size="sm"
                                                            className="rounded-pill px-3"
                                                            disabled={appt.payment.paymentStatus !== "PENDING"}
                                                            style={{ backgroundColor: "#20c997", borderColor: "#20c997" }}
                                                        >
                                                            Thanh Toán
                                                        </Button>
                                                    )}

                                                {user.role === "DOCTOR" && appt.status === "PENDING" && (
                                                    <Button
                                                    as={Link}
                                                    to={`/health-record/create/${appt.id}`}
                                                    variant="info"
                                                    size="sm"
                                                    className="rounded-pill px-3"
                                                    disabled={!user.isVerified}
                                                    style={{ backgroundColor: "#0dcaf0", borderColor: "#0dcaf0" }}
                                                    >
                                                    Tạo Kết Quả Khám
                                                    </Button>
                                                )}
                                                                                                
                                                {appt.status === "PENDING" && (
                                                    <Button
                                                        as={Link}
                                                        to={`/chat/${user.role === "PATIENT" ? appt.doctor.id : appt.patient.id}`}
                                                        variant="primary"
                                                        size="sm"
                                                        className="rounded-pill px-3 ms-2"
                                                        disabled={user && user.role === "DOCTOR" && !user.isVerified}
                                                        style={{ backgroundColor: "#0d6efd", borderColor: "#0d6efd" }}
                                                    >
                                                        Chat
                                                    </Button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        )}

                        {page > 0 && !loadingDoctors && !loadingAppointments && user?.role !== "DOCTOR" && (
                            <div className="text-center my-5">
                                <Button 
                                    variant="primary" 
                                    onClick={loadMore} 
                                    className="px-5 py-2 rounded-pill shadow-sm"
                                    style={{ 
                                        backgroundColor: "#0d6efd", 
                                        borderColor: "#0d6efd",
                                        transition: "transform 0.2s"
                                    }}
                                    onMouseEnter={(e) => e.target.style.transform = "scale(1.05)"}
                                    onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
                                >
                                    Xem Thêm
                                </Button>
                            </div>
                        )}
                    </>
                )}

                {/* Modal đổi lịch hẹn */}
                <Modal 
                    show={showRescheduleModal} 
                    onHide={closeRescheduleModal}
                    centered
                >
                    <Modal.Header 
                        closeButton 
                        className="bg-primary text-white"
                        style={{ borderTopLeftRadius: "10px", borderTopRightRadius: "10px" }}
                    >
                        <Modal.Title>Đổi Lịch Hẹn</Modal.Title>
                    </Modal.Header>
                    <Modal.Body className="p-4">
                        <Form>
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">Chọn Ngày Giờ Mới</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    value={newDateTime}
                                    onChange={(e) => setNewDateTime(e.target.value)}
                                    min={new Date().toISOString().slice(0, 16)}
                                    className="border-primary"
                                    style={{ borderRadius: "10px" }}
                                />
                            </Form.Group>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer className="border-0">
                        <Button 
                            variant="secondary" 
                            onClick={closeRescheduleModal}
                            className="rounded-pill px-4"
                            style={{ transition: "background 0.2s" }}
                        >
                            Đóng
                        </Button>
                        <Button
                            variant="primary"
                            onClick={rescheduleAppointment}
                            disabled={loadingReschedule || !newDateTime}
                            className="rounded-pill px-4"
                            style={{ backgroundColor: "#0d6efd", borderColor: "#0d6efd", transition: "background 0.2s" }}
                        >
                            {loadingReschedule ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Đang Xử Lý...
                                </>
                            ) : (
                                "Lưu Thay Đổi"
                            )}
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Container>
        </>
    );
};

export default Home;