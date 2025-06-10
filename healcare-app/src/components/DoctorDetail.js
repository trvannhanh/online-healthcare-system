import { useEffect, useState } from 'react';
import { Button, Container, Row, Col, Spinner, Alert, Form, Card } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import { FaStar, FaStarHalfAlt, FaRegStar, FaHospital, FaCalendarAlt } from 'react-icons/fa';
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

    const [rating, setRating] = useState(0);
    const [loadingRating, setLoadingRating] = useState(false);

    const [doctorRatings, setDoctorRatings] = useState([]);
    const [loadingDoctorRatings, setLoadingDoctorRatings] = useState(false);

    const loadDoctor = async () => {
        try {
            setLoading(true);
            let res = await Apis.get(`${endpoints['doctors']}/${id}`);
            setDoctor(res.data);

            fetchDoctorRatingAverage(id);
            fetchDoctorRatings(id);
        } catch (ex) {
            console.error(ex);
            setError(ex.response?.data || 'Không thể tải thông tin bác sĩ. Vui lòng thử lại sau.');
        } finally {
            setLoading(false);
        }
    };

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

    const bookAppointment = async () => {
        if (!user) {
            setError('Bạn cần đăng nhập để đặt lịch hẹn.');
            return;
        }
        if (!selectedSlot) {
            setError('Vui lòng chọn một khung giờ.');
            return;
        }

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
                errorMessage = ex.response.data; 
            }
            setError(errorMessage);
            setTimeout(() => setError(null), 5000); 
        } finally {
            setLoading(false);
        }
    };

    const fetchDoctorRatingAverage = async (doctorId) => {
        try {
            setLoadingRating(true);
            const response = await Apis.get(endpoints['doctorAverageRating'](doctorId));
            setRating(response.data);
        } catch (error) {
            console.error("Error fetching doctor rating:", error);
        } finally {
            setLoadingRating(false);
        }
    };

    const fetchDoctorRatings = async (doctorId) => {
        try {
            setLoadingDoctorRatings(true);
            const response = await Apis.get(endpoints['ratingForDoctor'](doctorId));
            const processedData = response.data.map(item => {
                if (typeof item === 'object' && item !== null) {
                    if (item.rating && typeof item.rating === 'object') {
                        return {
                            id: item.rating.id || item.id,
                            appointment: item.rating.appointment || {},
                            rating: item.rating.rating || 0,
                            comment: item.rating.comment || "Không có nhận xét",
                            ratingDate: item.rating.ratingDate || item.rating.createdAt,
                            response: item.response || null
                        };
                    } else {
                        return {
                            id: item.id || 0,
                            appointment: item.appointment || {},
                            rating: item.rating || 0,
                            comment: typeof item.comment === 'string' ? item.comment : "Không có nhận xét",
                            ratingDate: item.ratingDate || item.createdAt,
                            response: item.response || null
                        };
                    }
                }
                return null;
            }).filter(Boolean);

            console.log("Processed data:", processedData);
            setDoctorRatings(processedData);
        } catch (error) {
            console.error("Error fetching doctor ratings:", error);
            setDoctorRatings([]);

        } finally {
            setLoadingDoctorRatings(false);
        }
    };

    const formatDate = (dateString) => {
        const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
        return new Date(dateString).toLocaleString('vi-VN', options);
    };

    const renderStars = (rating) => {
        const stars = [];
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;

  
        for (let i = 0; i < fullStars; i++) {
            stars.push(<FaStar key={`full-${i}`} className="me-1" style={{ color: '#f1c40f' }} />);
        }

      
        if (hasHalfStar) {
            stars.push(<FaStarHalfAlt key="half" className="me-1" style={{ color: '#f1c40f' }} />);
        }

   
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (let i = 0; i < emptyStars; i++) {
            stars.push(<FaRegStar key={`empty-${i}`} className="me-1" style={{ color: '#f1c40f' }} />);
        }

        return stars;

    };

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
                        {renderStars(rating)}
                        <span className="fw-semibold ms-2" style={{ color: '#333' }}>
                            {rating ? rating.toFixed(1) : "N/A"} ({doctor?.experienceYears} năm kinh nghiệm)

                        </span>
                    </div>
                </Col>
            </Row>

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
            {/* Phần hiển thị đánh giá */}
            <Row className="mt-5">
                <Col md={12}>
                    <h3 style={{ color: '#1a3c34', fontWeight: 'bold', marginBottom: '20px' }}>
                        Đánh giá từ bệnh nhân
                    </h3>

                    {loadingDoctorRatings ? (
                        <div className="text-center my-4">
                            <Spinner animation="border" variant="primary" />
                            <p>Đang tải đánh giá...</p>
                        </div>
                    ) : doctorRatings.length > 0 ? (
                        <div>
                            {doctorRatings.map((item, index) => (
                                <Card key={index} className="mb-3 border-0 shadow-sm">
                                    <Card.Body>
                                        <div className="d-flex align-items-center mb-2">
                                            <img
                                                src={item.appointment?.patient?.user?.avatar || '/images/user-placeholder.jpg'}
                                                alt="Avatar"
                                                className="rounded-circle me-3"
                                                style={{ width: '50px', height: '50px', objectFit: 'cover' }}
                                            />
                                            <div>
                                                <h5 className="mb-0">
                                                    {item.appointment?.patient?.user?.firstName} {item.appointment?.patient?.user?.lastName || item.appointment?.patient?.user?.username || "Người dùng ẩn danh"}
                                                </h5>
                                                <small className="text-muted">{formatDate(item.ratingDate || item.createdAt)}</small>
                                            </div>
                                        </div>
                                        <div className="mb-2">
                                            {renderStars(item.rating || 0)}
                                            <span className="ms-2">({item.rating || 0}/5)</span>
                                        </div>
                                        <Card.Text style={{ color: '#555' }}>
                                            {item.comment || "Không có nhận xét."}
                                        </Card.Text>

                                        {/* Hiển thị phản hồi từ bác sĩ nếu có */}
                                        {item.response && (
                                            <div className="mt-3 p-3 bg-light rounded">
                                                <div className="d-flex align-items-center mb-2">
                                                    <img
                                                        src={doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                                                        alt="Doctor Avatar"
                                                        className="rounded-circle me-2"
                                                        style={{ width: '30px', height: '30px', objectFit: 'cover' }}
                                                    />
                                                    <div>
                                                        <h6 className="mb-0 text-primary">
                                                            BS. {doctor.user.firstName} {doctor.user.lastName}
                                                        </h6>
                                                        <small className="text-muted">{formatDate(item.response.responseDate || item.response.createdAt)}</small>
                                                    </div>
                                                </div>
                                                <Card.Text style={{ color: '#444' }}>
                                                    {item.response.content || ""}
                                                </Card.Text>
                                            </div>
                                        )}
                                    </Card.Body>
                                </Card>
                            ))}
                        </div>
                    ) : (
                        <Alert variant="light" className="text-center border">
                            Chưa có đánh giá nào cho bác sĩ này.
                        </Alert>
                    )}
                </Col>
            </Row>
        </Container>
    );
};

export default DoctorDetail;