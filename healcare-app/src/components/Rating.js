import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Alert, Spinner, Card } from 'react-bootstrap';
import { FaStar, FaRegStar } from 'react-icons/fa';
import { useParams, useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, Apis, endpoints } from '../configs/Apis';

const Rating = () => {
    const { appointmentId } = useParams();
    const { user } = useMyUser();
    const navigate = useNavigate();

    const [appointment, setAppointment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [canRate, setCanRate] = useState(false);
    const [existingRating, setExistingRating] = useState(null);

    // Form data
    const [rating, setRating] = useState(0);
    const [hoverRating, setHoverRating] = useState(0);
    const [comment, setComment] = useState('');
    const [ratingDate, setRatingDate] = useState(null); // Thêm state ratingDate

    // Fetch appointment data and check if user can rate
    useEffect(() => {
        const fetchData = async () => {
            if (!user || !appointmentId) {
                setError("Thông tin không hợp lệ hoặc bạn chưa đăng nhập");
                setLoading(false);
                return;
            }

            try {
                // Lấy thông tin cuộc hẹn
                // Cách đúng - sử dụng hàm endpoints['appointmentDetail'] với tham số
                const appointmentRes = await authApis().get(endpoints['appointmentDetail'](appointmentId));
                setAppointment(appointmentRes.data);

                // Kiểm tra nếu cuộc hẹn đã hoàn thành
                if (appointmentRes.data.status === 'COMPLETED') {
                    setCanRate(true);
                }

                // Kiểm tra xem đã có đánh giá chưa
                try {
                    const ratingsRes = await authApis().get(endpoints['appointmentRating'](appointmentId));
                    if (ratingsRes.data) {
                        setExistingRating(ratingsRes.data);
                        setRating(ratingsRes.rating);
                        setComment(ratingsRes.data.comment || '');
                        setRatingDate(ratingsRes.data.ratingDate || ratingsRes.data.createdAt);
                        const ratingDate = new Date(ratingsRes.data.ratingDate || ratingsRes.data.createdAt);
                        const now = new Date();
                        const diffInMinutes = (now - ratingDate) / (1000 * 60);

                        if (diffInMinutes > 30) {
                            setError("Không thể chỉnh sửa đánh giá sau 30 phút");
                            setCanRate(false);
                            setTimeout(() => navigate('/appointment'), 2000);
                        }
                    }
                } catch (err) {
                    console.error("Error fetching existing rating:", err);
                }
            } catch (err) {
                console.error("Error:", err);
                setError(err.response?.data?.error || "Không thể tải thông tin cuộc hẹn");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [appointmentId, user, navigate]);
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (rating === 0) {
            setError("Vui lòng chọn số sao đánh giá");
            return;
        }

        setSubmitting(true);
        setError(null);

        try {
            if (existingRating) {
                // Cập nhật đánh giá hiện có - GIỮ NGUYÊN TẤT CẢ DỮ LIỆU GỐC
                const updatedRating = {
                    ...existingRating,  // giữ tất cả thuộc tính gốc
                    rating: rating,      // chỉ cập nhật rating và comment
                    comment: comment
                };

                // Log rõ dữ liệu gửi đi để debug
                console.log("Dữ liệu cập nhật đánh giá:", JSON.stringify(updatedRating, null, 2));

                const response = await authApis().put(
                    endpoints['updateRating'](existingRating.id),
                    updatedRating
                );

                console.log("Kết quả cập nhật:", response.data);
                setSuccess("Cập nhật đánh giá thành công!");
            } else {
                // Tạo đánh giá mới
                const newRating = {
                    appointment: { id: parseInt(appointmentId) },
                    rating: rating,
                    comment: comment,
                    ratingDate: new Date().toISOString()
                };

                console.log("Dữ liệu đánh giá mới:", JSON.stringify(newRating, null, 2));

                await authApis().post(endpoints['addRating'], newRating);
                setSuccess("Đánh giá thành công!");
            }

            // Sau 2 giây, chuyển về trang danh sách đánh giá
            setTimeout(() => navigate('/pending-rating'), 2000);
        } catch (err) {
            console.error("Error submitting rating:", err);
            console.error("Response status:", err.response?.status);
            console.error("Response data:", err.response?.data);
            setError("Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại sau.");
        } finally {
            setSubmitting(false);
        }
    };

    // Hiển thị các sao để chọn đánh giá
    const renderStarRating = () => {
        return [1, 2, 3, 4, 5].map((star) => (
            <span
                key={star}
                className="fs-3 me-2"
                style={{ cursor: 'pointer' }}
                onClick={() => setRating(star)}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
            >
                {star <= (hoverRating || rating) ?
                    <FaStar style={{ color: '#f1c40f' }} /> :
                    <FaRegStar style={{ color: '#f1c40f' }} />}
            </span>
        ));
    };

    if (loading) {
        return (
            <Container className="my-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tải thông tin...</p>
            </Container>
        );
    }

    if (error && !appointment) {
        return (
            <Container className="my-5">
                <Alert variant="danger">{error}</Alert>
                <Button variant="secondary" onClick={() => navigate('/')}>Quay lại trang chủ</Button>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h1 className="text-center mb-4">Đánh giá bác sĩ</h1>

            {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
            {success && <Alert variant="success" dismissible onClose={() => setSuccess(null)}>{success}</Alert>}

            {appointment && (
                <Card className="shadow-sm mb-4">
                    <Card.Body>
                        <Row className="align-items-center">
                            <Col md={3} className="text-center">
                                <img
                                    src={appointment.doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                                    alt={`BS. ${appointment.doctor.user.firstName} ${appointment.doctor.user.lastName}`}
                                    className="img-fluid rounded-circle shadow"
                                    style={{ width: '150px', height: '150px', objectFit: 'cover' }}
                                />
                            </Col>
                            <Col md={9}>
                                <h3 className="text-primary">BS. {appointment.doctor.user.firstName} {appointment.doctor.user.lastName}</h3>
                                <p className="text-muted mb-1">
                                    <strong>Chuyên khoa:</strong> {appointment.doctor.specialization.name}
                                </p>
                                <p className="text-muted mb-1">
                                    <strong>Bệnh viện:</strong> {appointment.doctor.hospital.name}
                                </p>
                                <p className="text-muted mb-3">
                                    <strong>Ngày khám:</strong> {new Date(appointment.appointmentDate).toLocaleString('vi-VN')}
                                </p>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            )}

            <Card className="shadow-sm">
                <Card.Body>
                    {!canRate && !existingRating && (
                        <Alert variant="warning">
                            Bạn chỉ có thể đánh giá sau khi cuộc hẹn đã hoàn thành.
                        </Alert>
                    )}

                    {existingRating && (
                        <Alert variant="info">
                            Bạn đã đánh giá bác sĩ này. Bạn có thể cập nhật đánh giá của mình.
                        </Alert>
                    )}

                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-4 text-center">
                            <Form.Label className="d-block mb-3">Đánh giá của bạn</Form.Label>
                            <div>{renderStarRating()}</div>
                            <div className="mt-2">
                                {rating === 0 ? "Chọn đánh giá" : `${rating}/5 sao`}
                            </div>
                        </Form.Group>

                        <Form.Group className="mb-4">
                            <Form.Label>Nhận xét (tùy chọn)</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={4}
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                placeholder="Chia sẻ trải nghiệm của bạn với bác sĩ..."
                                disabled={!canRate && !existingRating}
                            />
                        </Form.Group>

                        <div className="d-grid gap-2">
                            <Button
                                variant="primary"
                                type="submit"
                                disabled={submitting || (!canRate && !existingRating)}
                            >
                                {submitting ? (
                                    <>
                                        <Spinner animation="border" size="sm" className="me-2" />
                                        Đang xử lý...
                                    </>
                                ) : existingRating ? "Cập nhật đánh giá" : "Gửi đánh giá"}
                            </Button>

                            <Button
                                variant="outline-secondary"
                                onClick={() => navigate('/')}
                            >
                                Quay lại trang chủ
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Rating;