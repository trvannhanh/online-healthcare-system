import { useEffect, useState } from "react";
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner, InputGroup, FormControl, NavDropdown, Table } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../configs/Apis";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import { useMyUser } from "../configs/MyContexts";
import { FaStar, FaStarHalfAlt, FaRegStar, FaSearch, FaUserMd, FaCalendarCheck, FaHospital, FaStethoscope } from 'react-icons/fa';
import { IoChatbubbleEllipsesOutline } from 'react-icons/io5';

const Home = () => {
    const [doctors, setDoctors] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [loadingDoctors, setLoadingDoctors] = useState(true);
    const [loadingAppointments, setLoadingAppointments] = useState(true);
    const [hasMoreDoctors, setHasMoreDoctors] = useState(true); // Thêm trạng thái
    const [loadingCancel, setLoadingCancel] = useState(false);
    const [loadingReschedule, setLoadingReschedule] = useState(false);
    const { user } = useMyUser() || {};
    const [page, setPage] = useState(1);
    const [q] = useSearchParams();
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showRescheduleModal, setShowRescheduleModal] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [newDateTime, setNewDateTime] = useState("");
    const [ratings, setRatings] = useState({});
    const [loadingRatings, setLoadingRatings] = useState(false);
    const [doctorName, setDoctorName] = useState("");
    const [hospitals, setHospitals] = useState([]);
    const [specialization, setSpecialization] = useState([]);
    const nav = useNavigate();
    const doctorRatings = {};

    const loadHospitals = async () => {
        try {
            let res = await Apis.get(endpoints["hospitals"]);
            setHospitals(res.data);
        } catch (ex) {
            console.error("Load hospitals error:", ex);
            setError("Không thể tải danh sách bệnh viện. Vui lòng thử lại sau.");
        }
    };

    const loadSpecialization = async () => {
        try {
            let res = await Apis.get(endpoints["specialization"]);
            setSpecialization(res.data);
        } catch (ex) {
            console.error("Load specialization error:", ex);
            setError("Không thể tải danh sách chuyên khoa. Vui lòng thử lại sau.");
        }
    };

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
            if (res.data.length === 0) {
                setHasMoreDoctors(false); // Không còn bác sĩ để tải
            } else {
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
            if (user.role === "PATIENT") url += `&patientId=${user.id}`;
            else if (user.role === "DOCTOR") url += `&doctorId=${user.id}`;

            const res = await authApis().get(url);
            const appointmentsData = res.data;
            const appointmentsWithPayment = await Promise.all(
                appointmentsData.map(async (appt) => {
                    if (appt.status === "COMPLETED") {
                        try {
                            const paymentRes = await Apis.get(`${endpoints["payment"]}/appointment/${appt.id}`);
                            return { ...appt, payment: paymentRes.data };
                        } catch (ex) {
                            if (ex.response?.status === 404) return { ...appt, payment: null };
                            throw ex;
                        }
                    }
                    return { ...appt, payment: null };
                })
            );

            if (page === 1) setAppointments(appointmentsWithPayment);
            else setAppointments([...appointments, ...appointmentsWithPayment]);
        } catch (ex) {
            console.error("Load appointments error:", ex);
            setError(ex.response?.status === 403
                ? "Bạn không có quyền truy cập lịch hẹn. Tài khoản bác sĩ chưa được xác nhận."
                : `Không thể tải danh sách lịch hẹn: ${ex.message || ex}`);
        } finally {
            setLoadingAppointments(false);
        }
    };

    useEffect(() => {
        loadHospitals();
        loadSpecialization();
    }, []);

    // Tách useEffect cho loadDoctors
    useEffect(() => {
        if (user?.role === "PATIENT" && page > 0 && hasMoreDoctors) {
            loadDoctors();
        }
    }, [page, q, user?.id]);

    // Tách useEffect cho loadAppointments
    useEffect(() => {
        if (user && page > 0) {
            loadAppointments();
        }
    }, [page, user?.id, user?.role, user?.isVerified]);

    useEffect(() => {
        setPage(1);
        setDoctors([]);
        setAppointments([]);
        setHasMoreDoctors(true); // Reset khi thay đổi query
    }, [q]);

    const loadMore = () => {
        if (!loadingDoctors && hasMoreDoctors) {
            setPage(page + 1);
        }
    };

    const formatDate = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString("vi-VN", { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" });
    };

    const cancelAppointment = async (appointmentId) => {
        if (!window.confirm("Bạn có chắc chắn muốn hủy lịch hẹn này?")) return;
        try {
            setLoadingCancel(true);
            const url = endpoints["cancelAppointment"](appointmentId);
            await authApis().patch(url);
            await loadAppointments();
            setSuccess("Hủy lịch hẹn thành công!");
            setTimeout(() => setSuccess(null), 2000);
        } catch (ex) {
            console.error("Cancel error:", ex);
            setError(`Hủy lịch hẹn thất bại: ${ex.response?.data || ex.response?.statusText || ex.message}`);
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
            await authApis().patch(url, body);
            await loadAppointments();
            setSuccess("Đổi lịch hẹn thành công!");
            setTimeout(() => setSuccess(null), 2000);
            closeRescheduleModal();
        } catch (ex) {
            console.error("Reschedule error:", ex);
            setError(`Đổi lịch hẹn thất bại: ${ex.response?.data || ex.response?.statusText || ex.message}`);
        } finally {
            setLoadingReschedule(false);
        }
    };

    const renderStars = (rating) => {
        if (!rating && rating !== 0) return null;
        const stars = [];
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        for (let i = 0; i < fullStars; i++) {
            stars.push(<FaStar key={`full-${i}`} className="me-1" style={{ color: '#f1c40f', fontSize: '0.9rem' }} />);
        }
        if (hasHalfStar) {
            stars.push(<FaStarHalfAlt key="half" className="me-1" style={{ color: '#f1c40f', fontSize: '0.9rem' }} />);
        }
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

    const fetchDoctorRatings = async (doctorsToFetch) => {
        if (doctorsToFetch.length === 0) return;
        setLoadingRatings(true);
        const newRatings = { ...ratings };
        try {
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

    const search = (e) => {
        e.preventDefault();
        nav(`/?doctorName=${doctorName}`);
    };

    const checkVerified = (e, hospitalName, specializationName) => {
        if (user && user.role === "DOCTOR" && !user.isVerified) {
            e.preventDefault();
            alert("Tài khoản của bạn chưa được xác nhận giấy phép hành nghề. Vui lòng chờ quản trị viên xác nhận.");
            return false;
        }
        if (hospitalName) nav(`/?hospital=${hospitalName}`);
        if (specializationName) nav(`/?specialization=${specializationName}`);
        return true;
    };

    return (
        <>
            {/* Hero Section */}
            <div
                className="text-white py-5 px-4 mb-5 shadow-lg"
                style={{
                    background: "linear-gradient(135deg, #0d6efd, #20c997), url('/images/hero-doctor.jpg') center/cover no-repeat",
                    borderRadius: "20px",
                    minHeight: "350px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    textAlign: "center",
                    animation: "fadeIn 1s ease-in"
                }}
            >
                <Container>
                    <h1 className="display-4 fw-bold mb-4" style={{ textShadow: "2px 2px 6px rgba(0,0,0,0.4)" }}>
                        Chăm Sóc Sức Khỏe Dễ Dàng
                    </h1>
                    <p className="lead mb-5" style={{ fontSize: "1.25rem", fontWeight: "300" }}>
                        Kết nối với bác sĩ hàng đầu và đặt lịch khám chỉ trong vài phút.
                    </p>
                    <Button
                        as={Link}
                        to="/appointment"
                        variant="success"
                        className="px-5 py-2 rounded-pill shadow-sm"
                        style={{ backgroundColor: "#20c997", borderColor: "#20c997", fontSize: "1.1rem", transition: "transform 0.2s" }}
                        onMouseEnter={(e) => e.target.style.transform = "scale(1.05)"}
                        onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
                    >
                        <FaCalendarCheck className="me-2" /> Đặt Lịch Ngay
                    </Button>
                </Container>
            </div>

            <Container className="py-4">
                {error && (
                    <Alert variant="danger" onClose={() => setError(null)} dismissible className="shadow-sm rounded-pill px-4 py-3">
                        {error}
                    </Alert>
                )}
                {success && (
                    <Alert variant="success" onClose={() => setSuccess(null)} dismissible className="shadow-sm rounded-pill px-4 py-3">
                        {success}
                    </Alert>
                )}

                {/* Search Bar */}
                {user?.role === "PATIENT" && (
                    <div className="mb-5">
                        <h3 className="fw-bold text-primary mb-4">Tìm Kiếm Bác Sĩ</h3>
                        <Form onSubmit={search} className="d-flex align-items-center gap-3 flex-wrap">
                            <InputGroup className="shadow-sm" style={{ maxWidth: "400px" }}>
                                <FormControl
                                    type="text"
                                    placeholder="Nhập tên bác sĩ..."
                                    value={doctorName}
                                    onChange={(e) => setDoctorName(e.target.value)}
                                    className="border-0 rounded-start-pill"
                                    style={{ background: '#fff', color: '#333' }}
                                />
                                <Button 
                                    type="submit" 
                                    variant="primary" 
                                    className="rounded-end-pill"
                                    style={{ backgroundColor: "#20c997", borderColor: "#20c997" }}
                                >
                                    <FaSearch />
                                </Button>
                            </InputGroup>
                            <NavDropdown 
                                title={
                                    <span className="fw-semibold text-primary">
                                        <FaHospital className="me-1" /> Bệnh Viện
                                    </span>
                                } 
                                className="px-3 py-2 rounded-pill shadow-sm"
                                style={{ 
                                    backgroundColor: "#fff",
                                    transition: 'background 0.2s',
                                    cursor: 'pointer'
                                }}
                                onMouseEnter={(e) => e.target.style.background = 'rgba(13,110,253,0.1)'}
                                onMouseLeave={(e) => e.target.style.background = '#fff'}
                            >
                                {hospitals.length === 0 ? (
                                    <NavDropdown.Item disabled>Đang tải...</NavDropdown.Item>
                                ) : (
                                    hospitals.map(h => (
                                        <NavDropdown.Item 
                                            key={h.id} 
                                            className="py-2 bg-white text-dark hover:bg-teal-100"
                                            onClick={(e) => checkVerified(e, h.name)}
                                        >
                                            {h.name}
                                        </NavDropdown.Item>
                                    ))
                                )}
                            </NavDropdown>
                            <NavDropdown 
                                title={
                                    <span className="fw-semibold text-primary">
                                        <FaStethoscope className="me-1" /> Chuyên Khoa
                                    </span>
                                } 
                                className="px-3 py-2 rounded-pill shadow-sm"
                                style={{ 
                                    backgroundColor: "#fff",
                                    transition: 'background 0.2s',
                                    cursor: 'pointer'
                                }}
                                onMouseEnter={(e) => e.target.style.background = 'rgba(13,110,253,0.1)'}
                                onMouseLeave={(e) => e.target.style.background = '#fff'}
                            >
                                {specialization.length === 0 ? (
                                    <NavDropdown.Item disabled>Đang tải...</NavDropdown.Item>
                                ) : (
                                    specialization.map(s => (
                                        <NavDropdown.Item 
                                            key={s.id} 
                                            className="py-2 bg-white text-dark hover:bg-teal-100"
                                            onClick={(e) => checkVerified(e, null, s.name)}
                                        >
                                            {s.name}
                                        </NavDropdown.Item>
                                    ))
                                )}
                            </NavDropdown>
                        </Form>
                    </div>
                )}

                {/* Guest User Section */}
                {!user && (
                    <div className="mb-5">
                        <h2 className="text-center fw-bold text-primary mb-4">Tại Sao Chọn HealCare?</h2>
                        <Row className="g-4">
                            <Col md={4}>
                                <Card className="h-100 border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                    <FaUserMd className="text-primary mb-3" style={{ fontSize: "3rem" }} />
                                    <Card.Title className="fw-bold">Bác Sĩ Uy Tín</Card.Title>
                                    <Card.Text>
                                        Hợp tác với các bác sĩ được chứng nhận từ các bệnh viện hàng đầu.
                                    </Card.Text>
                                </Card>
                            </Col>
                            <Col md={4}>
                                <Card className="h-100 border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                    <FaCalendarCheck className="text-primary mb-3" style={{ fontSize: "3rem" }} />
                                    <Card.Title className="fw-bold">Đặt Lịch Nhanh</Card.Title>
                                    <Card.Text>
                                        Đặt lịch khám dễ dàng với giao diện thân thiện và tiện lợi.
                                    </Card.Text>
                                </Card>
                            </Col>
                            <Col md={4}>
                                <Card className="h-100 border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                    <IoChatbubbleEllipsesOutline className="text-primary mb-3" style={{ fontSize: "3rem" }} />
                                    <Card.Title className="fw-bold">Hỗ Trợ 24/7</Card.Title>
                                    <Card.Text>
                                        Đội ngũ hỗ trợ luôn sẵn sàng giải đáp mọi thắc mắc.
                                    </Card.Text>
                                </Card>
                            </Col>
                        </Row>
                        <div className="text-center mt-5">
                            <Button
                                as={Link}
                                to="/register"
                                variant="success"
                                className="px-5 py-2 rounded-pill shadow-sm"
                                style={{ backgroundColor: "#20c997", borderColor: "#20c997" }}
                            >
                                Tham Gia Ngay
                            </Button>
                        </div>
                    </div>
                )}

                {/* Doctor Dashboard */}
                {user?.role === "DOCTOR" && (
                    <div className="mb-5">
                        <h2 className="text-center fw-bold text-primary mb-4">
                            {user.isVerified ? `Chào Bác Sĩ ${user.firstName}!` : "Chào Bác Sĩ!"}
                        </h2>
                        {user.isVerified ? (
                            <Row className="g-4">
                                <Col md={4}>
                                    <Card className="border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                        <FaCalendarCheck className="text-primary mb-3" style={{ fontSize: "2.5rem" }} />
                                        <Card.Title>Quản Lý Lịch Hẹn</Card.Title>
                                        <Card.Text>Xem và quản lý các lịch hẹn của bạn.</Card.Text>
                                        <Button as={Link} to="/appointment" variant="outline-primary" className="rounded-pill">
                                            Xem Lịch Hẹn
                                        </Button>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                        <FaUserMd className="text-primary mb-3" style={{ fontSize: "2.5rem" }} />
                                        <Card.Title>Hồ Sơ Bác Sĩ</Card.Title>
                                        <Card.Text>Cập nhật thông tin cá nhân và chuyên môn.</Card.Text>
                                        <Button as={Link} to="/profile" variant="outline-primary" className="rounded-pill">
                                            Cập Nhật Hồ Sơ
                                        </Button>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="border-0 shadow-sm text-center p-4" style={{ borderRadius: "15px" }}>
                                        <IoChatbubbleEllipsesOutline className="text-primary mb-3" style={{ fontSize: "2.5rem" }} />
                                        <Card.Title>Chat Với Bệnh Nhân</Card.Title>
                                        <Card.Text>Giao tiếp trực tiếp với bệnh nhân của bạn.</Card.Text>
                                        <Button as={Link} to="/chat" variant="outline-primary" className="rounded-pill">
                                            Mở Chat
                                        </Button>
                                    </Card>
                                </Col>
                            </Row>
                        ) : (
                            <Alert variant="warning" className="shadow-sm rounded-pill px-4 py-3 text-center">
                                Giấy phép hành nghề của bạn chưa được xác nhận. Vui lòng chờ quản trị viên xác minh.
                                <div className="mt-3">
                                    <Button as={Link} to="/contact" variant="outline-primary" className="rounded-pill">
                                        Liên Hệ Hỗ Trợ
                                    </Button>
                                </div>
                            </Alert>
                        )}
                    </div>
                )}

                {/* Patient Section */}
                {user?.role === "PATIENT" && (
                    <>
                        {/* Doctors List */}
                        <h3 className="fw-bold text-primary mb-4">Danh Sách Bác Sĩ</h3>
                        {loadingDoctors && (
                            <div className="text-center my-5">
                                <Spinner animation="border" variant="primary" style={{ width: "3rem", height: "3rem" }} />
                            </div>
                        )}
                        {doctors.length === 0 && !loadingDoctors && (
                            <Container className="py-5 text-center">
                                <Alert variant="info" className="shadow-sm rounded-pill px-4 py-3">
                                    Không tìm thấy bác sĩ phù hợp với tiêu chí của bạn.
                                </Alert>
                                <p className="lead mb-4">Hãy thử thay đổi tiêu chí tìm kiếm hoặc khám phá các chuyên khoa khác.</p>
                                <Button as={Link} to="/appointment" variant="success" className="px-5 py-2 rounded-pill shadow-sm">
                                    Tìm Bác Sĩ
                                </Button>
                            </Container>
                        )}
                        <Row className="g-4">
                            {doctors.map((d) => (
                                <Col key={d.id} className="mb-4" md={4} lg={3} sm={6}>
                                    <Card
                                        className="h-100 shadow-lg border-0"
                                        style={{ borderRadius: "20px", overflow: "hidden", transition: "transform 0.3s, box-shadow 0.3s" }}
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
                                            style={{ width: "100%", height: "250px", objectFit: "cover" }}
                                        />
                                        <Card.Body className="d-flex flex-column justify-content-between p-4">
                                            <div>
                                                <Card.Title className="fs-5 fw-bold text-primary mb-3">
                                                    {d.user.firstName} {d.user.lastName}
                                                </Card.Title>
                                                <Card.Text className="mb-2" style={{ fontSize: "0.95rem" }}>
                                                    <FaHospital className="me-2" /> {d.hospital.name}
                                                </Card.Text>
                                                <Card.Text className="mb-3" style={{ fontSize: "0.95rem" }}>
                                                    <FaUserMd className="me-2" /> {d.specialization.name}
                                                </Card.Text>
                                                <div className="mb-2">
                                                    {ratings[d.id] !== undefined ? renderStars(ratings[d.id]) : <Spinner size="sm" />}
                                                </div>
                                            </div>
                                            <div className="mt-auto d-flex justify-content-between gap-2">
                                                <Button as={Link} to={`/doctors/${d.id}`} variant="outline-primary" size="sm" className="rounded-pill flex-grow-1">
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
                        {user?.role === "PATIENT" && hasMoreDoctors && !loadingDoctors && (
                            <div className="text-center my-5">
                                <Button
                                    variant="primary"
                                    onClick={loadMore}
                                    disabled={loadingDoctors}
                                    className="px-5 py-2 rounded-pill shadow-sm"
                                    style={{ backgroundColor: "#0d6efd", borderColor: "#0d6efd", transition: "transform 0.2s" }}
                                    onMouseEnter={(e) => e.target.style.transform = "scale(1.05)"}
                                    onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
                                >
                                    {loadingDoctors ? (
                                        <>
                                            <Spinner animation="border" size="sm" className="me-2" />
                                            Đang Tải...
                                        </>
                                    ) : (
                                        "Xem Thêm"
                                    )}
                                </Button>
                            </div>
                        )}
                        {user?.role === "PATIENT" && !hasMoreDoctors && doctors.length > 0 && (
                            <div className="text-center my-5">
                                <Alert variant="info" className="shadow-sm rounded-pill px-4 py-3">
                                    Đã tải toàn bộ danh sách bác sĩ.
                                </Alert>
                            </div>
                        )}
                    </>
                )}

                {/* Appointments Section */}
                {user && (
                    <>
                        <h3 className="fw-bold text-primary mb-4 mt-5">Lịch Hẹn Của Bạn</h3>
                        {loadingAppointments && page === 1 && (
                            <div className="text-center my-5">
                                <Spinner animation="border" variant="primary" style={{ width: "3rem", height: "3rem" }} />
                            </div>
                        )}
                        {appointments.length === 0 && !loadingAppointments && (
                            <Container className="py-5 text-center">
                                <Alert variant="info" className="shadow-sm rounded-pill px-4 py-3">
                                    Bạn chưa có lịch hẹn nào!
                                </Alert>
                                {user.role === "PATIENT" && (
                                    <Button as={Link} to="/appointment" variant="success" className="px-5 py-2 rounded-pill shadow-sm">
                                        Đặt Lịch Ngay
                                    </Button>
                                )}
                            </Container>
                        )}
                        {appointments.length > 0 && (
                            <Table striped bordered hover responsive className="mt-4 shadow-sm" style={{ borderRadius: "10px", overflow: "hidden" }}>
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
                                                    disabled={appt.status === 'COMPLETED' || appt.status === 'CANCELLED'}
                                                >
                                                    Đổi Lịch
                                                </Button>
                                                <Button
                                                    variant="danger"
                                                    size="sm"
                                                    className="rounded-pill px-3"
                                                    onClick={() => cancelAppointment(appt.id)}
                                                    disabled={appt.status !== "PENDING" && appt.status !== "CONFIRMED"}
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
                                                {user.role === "DOCTOR" && appt.status === "COMPLETED" && !appt.payment && (
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
                                                {user.role === "PATIENT" && appt.status === "COMPLETED" && appt.payment && appt.payment.paymentStatus !== "SUCCESSFUL" && (
                                                    <Button
                                                        as={Link}
                                                        to={`/payment/${appt.id}`}
                                                        variant="success"
                                                        size="sm"
                                                        className="rounded-pill px-3"
                                                        disabled={appt.payment.paymentStatus === "SUCCESSFULL"}
                                                        style={{ backgroundColor: "#20c997", borderColor: "#20c997" }}
                                                    >
                                                        Thanh Toán
                                                    </Button>
                                                )}
                                                {user.role === "DOCTOR" && (appt.status === "PENDING" || appt.status === "CONFIRMED") && (
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
                                                {(appt.status === "PENDING" || appt.status === "CONFIRMED") && (
                                                    <Button
                                                        as={Link}
                                                        to={`/chat/${user.role === "PATIENT" ? appt.doctor.user.id : appt.patient.id}`}
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
                    </>
                )}

                {/* Modal đổi lịch hẹn */}
                <Modal show={showRescheduleModal} onHide={closeRescheduleModal} centered>
                    <Modal.Header closeButton className="bg-primary text-white" style={{ borderTopLeftRadius: "10px", borderTopRightRadius: "10px" }}>
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
                        <Button variant="secondary" onClick={closeRescheduleModal} className="rounded-pill px-4">
                            Đóng
                        </Button>
                        <Button
                            variant="primary"
                            onClick={rescheduleAppointment}
                            disabled={loadingReschedule || !newDateTime}
                            className="rounded-pill px-4"
                            style={{ backgroundColor: "#0d6efd", borderColor: "#0d6efd" }}
                        >
                            {loadingReschedule ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Đang Xử Lý...
                                </>
                            ) : (
                                "Lưu Thay đổi"
                            )}
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Container>

            {/* CSS Animations */}
            <style>
                {`
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                `}
            </style>
        </>
    );
};

export default Home;