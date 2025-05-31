import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Alert, Spinner, Card } from 'react-bootstrap';
import { FaStar, FaRegStar } from 'react-icons/fa';
import { useParams, useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';
const Response = () => {
    const { ratingId } = useParams();
    const { user } = useMyUser();
    const navigate = useNavigate();

    const [rating, setRating] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [existingResponse, setExistingResponse] = useState(null);

    // Form data
    const [responseContent, setResponseContent] = useState('');

    useEffect(() => {
        const fetchData = async () => {
            if (!user || !ratingId) {
                setError("Thông tin không hợp lệ hoặc bạn chưa đăng nhập");
                setLoading(false);
                return;
            }

            if (user.role !== 'DOCTOR') {
                setError("Chỉ bác sĩ mới có quyền phản hồi đánh giá");
                setLoading(false);
                return;
            }

            try {
                // Lấy thông tin đánh giá
                const ratingRes = await authApis().get(endpoints['ratingById'](ratingId));
                setRating(ratingRes.data);

                // Kiểm tra xem đánh giá có phải dành cho bác sĩ này hay không
                if (ratingRes.data.appointment.doctor.id !== user.id) {
                    setError("Bạn không có quyền phản hồi đánh giá này");
                    setLoading(false);
                    return;
                }

                // Kiểm tra xem đã có phản hồi chưa
                try {
                    // Sử dụng endPoint dạng hàm nếu nó được định nghĩa là hàm
                    const responseRes = await authApis().get(endpoints['responseByRating'](ratingId));
                    if (responseRes.data) {
                        setExistingResponse(responseRes.data);
                        setResponseContent(responseRes.data.content || '');
                    }
                } catch (err) {
                    // Không có phản hồi - điều này là bình thường
                    console.log("Chưa có phản hồi cho đánh giá này");
                }
            } catch (err) {
                console.error("Error:", err);
                setError(err.response?.data?.error || "Không thể tải thông tin đánh giá");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [ratingId, user]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!responseContent.trim()) {
            setError("Vui lòng nhập nội dung phản hồi");
            return;
        }

        setSubmitting(true);
        setError(null);

        try {
            // Chuẩn bị dữ liệu phản hồi
            const responseData = {
                rating: { id: parseInt(ratingId) },
                content: responseContent,
                responseDate: new Date().toISOString()
            };

            if (existingResponse) {
                // Cập nhật phản hồi hiện có
                responseData.id = existingResponse.id;
                // Sử dụng endpoint dưới dạng hàm nếu được định nghĩa như vậy
                await authApis().put(endpoints['updateResponse'](existingResponse.id), responseData);

                setSuccess("Cập nhật phản hồi thành công!");
            } else {
                // Tạo phản hồi mới
                responseData.responseDate = new Date().toISOString();
                await authApis().post(endpoints['addResponse'], responseData);
                setSuccess("Phản hồi thành công!");
            }

            // Sau 2 giây, chuyển về trang quản lý đánh giá
            setTimeout(() => navigate('/doctor/ratings'), 2000);
        } catch (err) {
            console.error("Error submitting response:", err);
            setError(err.response?.data?.error || "Có lỗi xảy ra khi gửi phản hồi. Vui lòng thử lại sau.");
        } finally {
            setSubmitting(false);
        }
    };

    // Hiển thị các sao đánh giá (chỉ để hiển thị, không thể tương tác)
    const renderStars = (score) => {
        return [1, 2, 3, 4, 5].map((star) => (
            <span key={star} className="fs-5 me-1">
                {star <= score ?
                    <FaStar style={{ color: '#f1c40f' }} /> :
                    <FaRegStar style={{ color: '#f1c40f' }} />}
            </span>
        ));
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString('vi-VN');
    };

    if (loading) {
        return (
            <Container className="my-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tải thông tin...</p>
            </Container>
        );
    }

    if (error && !rating) {
        return (
            <Container className="my-5">
                <Alert variant="danger">{error}</Alert>
                <Button variant="secondary" onClick={() => navigate('/doctor/ratings')}>Quay lại</Button>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h1 className="text-center mb-4">Phản hồi đánh giá</h1>

            {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
            {success && <Alert variant="success" dismissible onClose={() => setSuccess(null)}>{success}</Alert>}

            {rating && (
                <Card className="shadow-sm mb-4">
                    <Card.Header as="h5">Thông tin đánh giá</Card.Header>
                    <Card.Body>
                        <Row>
                            <Col md={4}>
                                <p><strong>Bệnh nhân:</strong> {rating.appointment.patient.user.firstName} {rating.appointment.patient.user.lastName}</p>
                                <p><strong>Ngày khám:</strong> {formatDate(rating.appointment.appointmentDate)}</p>
                                <p><strong>Ngày đánh giá:</strong> {formatDate(rating.ratingDate || rating.createdAt)}</p>
                            </Col>
                            <Col md={8}>
                                <div className="mb-3">
                                    <strong>Đánh giá:</strong> {renderStars(rating.rating)} <span className="ms-2">({rating.rating}/5)</span>
                                </div>
                                <div className="mb-3">
                                    <strong>Nhận xét:</strong>
                                    <p className="p-3 bg-light rounded">{rating.comment || "Không có nhận xét."}</p>
                                </div>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            )}

            <Card className="shadow-sm">
                <Card.Header as="h5">Phản hồi của bạn</Card.Header>
                <Card.Body>
                    {existingResponse && (
                        <Alert variant="info">
                            Bạn đã phản hồi đánh giá này vào: {formatDate(existingResponse.responseDate || existingResponse.createdAt)}
                        </Alert>
                    )}

                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-4">
                            <Form.Label>Nội dung phản hồi</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={4}
                                value={responseContent}
                                onChange={(e) => setResponseContent(e.target.value)}
                                placeholder="Nhập phản hồi của bạn đối với đánh giá này..."
                            />
                        </Form.Group>

                        <div className="d-flex gap-2">
                            <Button
                                variant="primary"
                                type="submit"
                                disabled={submitting}
                                className="me-2"
                            >
                                {submitting ? (
                                    <>
                                        <Spinner animation="border" size="sm" className="me-2" />
                                        Đang xử lý...
                                    </>
                                ) : existingResponse ? "Cập nhật phản hồi" : "Gửi phản hồi"}
                            </Button>

                            <Button
                                variant="outline-secondary"
                                onClick={() => navigate('/doctor/ratings')}
                            >
                                Quay lại
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Response;