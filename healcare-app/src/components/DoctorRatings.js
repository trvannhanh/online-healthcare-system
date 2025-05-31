import React, { useState, useEffect } from 'react';
import { Container, Alert, Card, Button, Spinner, Row, Col, Badge, Nav } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';
import { FaStar, FaReply, FaCheck, FaExclamationTriangle } from 'react-icons/fa';

const DoctorRatings = () => {
    const { user } = useMyUser();
    const navigate = useNavigate();
    const [ratings, setRatings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    // Thêm state để lưu trạng thái filter
    const [filterStatus, setFilterStatus] = useState('all');

    // Kiểm tra xem rating có phản hồi không
    const hasResponse = (rating) => {
        // Nếu rating có thuộc tính hasResponded thì sử dụng nó
        if (rating.hasResponded !== undefined) {
            return rating.hasResponded;
        }

        // Hoặc kiểm tra responses
        if (rating.responses && Array.isArray(rating.responses)) {
            return rating.responses.length > 0;
        }

        // Hoặc nếu có một response object
        if (rating.response && typeof rating.response === 'object') {
            return true;
        }

        return false;
    };

    // Trong useEffect, cải thiện cách tải ratings:
    useEffect(() => {
        const loadRatings = async () => {
            if (!user || user.role !== 'DOCTOR') {
                setError("Bạn không có quyền truy cập trang này");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                // Lấy danh sách đánh giá
                const response = await authApis().get(endpoints['doctorRatings'](user.id));

                // Tạo một bản sao ratings để cập nhật trạng thái phản hồi
                const ratingsWithResponseCheck = [...response.data];

                // Kiểm tra từng đánh giá có phản hồi không
                for (let i = 0; i < ratingsWithResponseCheck.length; i++) {
                    try {
                        const responseCheck = await authApis().get(endpoints['responseByRating'](ratingsWithResponseCheck[i].id));
                        // Nếu có phản hồi, thêm thuộc tính hasResponded = true
                        if (responseCheck.data) {
                            ratingsWithResponseCheck[i].hasResponded = true;
                            ratingsWithResponseCheck[i].response = responseCheck.data;
                        } else {
                            ratingsWithResponseCheck[i].hasResponded = false;
                        }
                    } catch (err) {
                        // Nếu lỗi, giả định là chưa có phản hồi
                        ratingsWithResponseCheck[i].hasResponded = false;
                    }
                }

                setRatings(ratingsWithResponseCheck);
            } catch (err) {
                console.error("Error loading ratings:", err);
                setError("Không thể tải danh sách đánh giá: " + (err.response?.data || err.message));
            } finally {
                setLoading(false);
            }
        };

        loadRatings();
    }, [user]);

    // Lọc ratings theo trạng thái phản hồi
    const filteredRatings = ratings.filter(rating => {
        switch (filterStatus) {
            case 'responded':
                return hasResponse(rating);
            case 'unresponded':
                return !hasResponse(rating);
            default:
                return true; // all
        }
    });

    // Đếm số lượng phần tử trong mỗi danh mục
    const respondedCount = ratings.filter(r => hasResponse(r)).length;
    const unrespondedCount = ratings.filter(r => !hasResponse(r)).length;

    const formatDate = (dateString) => {
        if (!dateString) return "N/A";
        return new Date(dateString).toLocaleDateString('vi-VN');
    };

    // Hiển thị số sao đánh giá
    const renderStars = (score) => {
        return Array(score).fill(0).map((_, i) => (
            <FaStar key={i} className="text-warning" />
        ));
    };

    // Thêm hàm kiểm tra xem phản hồi còn trong thời gian cho phép chỉnh sửa không (1 giờ)
    const isWithinEditWindow = (rating) => {
        // Nếu không có phản hồi thì không cần kiểm tra
        if (!hasResponse(rating)) {
            return false;
        }

        // Lấy thời gian của phản hồi
        let responseDate;
        if (rating.responses && Array.isArray(rating.responses) && rating.responses.length > 0) {
            responseDate = new Date(rating.responses[0].responseDate);
        } else if (rating.response && rating.response.responseDate) {
            responseDate = new Date(rating.response.responseDate);
        } else {
            return false; // Không tìm thấy thời gian phản hồi
        }

        // Tính thời gian hiện tại
        const now = new Date();

        // Tính khoảng thời gian đã trôi qua (mili giây)
        const timeDiff = now - responseDate;

        // Chuyển đổi sang giờ (1 giờ = 3600000 mili giây)
        const hoursDiff = timeDiff / 3600000;

        // Cho phép chỉnh sửa nếu thời gian < 1 giờ
        return hoursDiff <= 1;
    };


    if (!user || user.role !== 'DOCTOR') {
        return (
            <Container className="my-5">
                <Alert variant="warning">
                    Trang này chỉ dành cho bác sĩ. Vui lòng đăng nhập với tài khoản bác sĩ.
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h2 className="mb-4 text-center">Đánh giá của bệnh nhân</h2>

            {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}

            {/* Thêm Tab Navigation cho phân loại */}
            <Nav variant="tabs" className="mb-4">
                <Nav.Item>
                    <Nav.Link
                        active={filterStatus === 'all'}
                        onClick={() => setFilterStatus('all')}
                    >
                        Tất cả ({ratings.length})
                    </Nav.Link>
                </Nav.Item>
                <Nav.Item>
                    <Nav.Link
                        active={filterStatus === 'responded'}
                        onClick={() => setFilterStatus('responded')}
                        className="text-success"
                    >
                        <FaCheck className="me-1" /> Đã phản hồi ({respondedCount})
                    </Nav.Link>
                </Nav.Item>
                <Nav.Item>
                    <Nav.Link
                        active={filterStatus === 'unresponded'}
                        onClick={() => setFilterStatus('unresponded')}
                        className="text-danger"
                    >
                        <FaExclamationTriangle className="me-1" /> Chưa phản hồi ({unrespondedCount})
                    </Nav.Link>
                </Nav.Item>
            </Nav>

            {loading ? (
                <div className="text-center my-5">
                    <Spinner animation="border" variant="primary" />
                    <p className="mt-2">Đang tải dữ liệu...</p>
                </div>
            ) : (
                <>
                    {filteredRatings.length === 0 ? (
                        <Alert variant="info">
                            {filterStatus === 'all' ? 'Bạn chưa có đánh giá nào.' :
                                filterStatus === 'responded' ? 'Không có đánh giá nào đã phản hồi.' :
                                    'Không có đánh giá nào chưa phản hồi.'}
                        </Alert>
                    ) : (
                        <Row xs={1} md={2} className="g-4">
                            {filteredRatings.map((rating) => (
                                <Col key={rating.id}>
                                    <Card className={`h-100 shadow-sm ${!hasResponse(rating) ? 'border-danger' : ''}`}>
                                        <Card.Header>
                                            <div className="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <span className="me-2">Đánh giá:</span>
                                                    {renderStars(rating.rating)}
                                                </div>
                                                <Badge bg={hasResponse(rating) ? "success" : "danger"}>
                                                    {hasResponse(rating) ? (
                                                        <><FaCheck className="me-1" /> Đã phản hồi</>
                                                    ) : (
                                                        <><FaExclamationTriangle className="me-1" /> Chưa phản hồi</>
                                                    )}
                                                </Badge>
                                            </div>
                                        </Card.Header>
                                        <Card.Body>
                                            <div className="mb-3">
                                                <small className="text-muted">Bệnh nhân: {rating.appointment.patient.user.firstName} {rating.appointment.patient.user.lastName}</small>
                                            </div>
                                            <Card.Title>Đánh giá ngày {formatDate(rating.ratingDate)}</Card.Title>
                                            <Card.Text>
                                                {rating.comment || "Không có nhận xét."}
                                            </Card.Text>

                                            {hasResponse(rating) ? (
                                                <div className="mt-3 p-3 bg-light rounded">
                                                    <strong>Phản hồi của bạn:</strong>
                                                    <p className="mb-0">
                                                        {rating.responses && Array.isArray(rating.responses) && rating.responses.length > 0
                                                            ? rating.responses[0].content
                                                            : rating.response?.content}
                                                    </p>
                                                    <small className="text-muted">
                                                        Phản hồi vào: {
                                                            rating.responses && Array.isArray(rating.responses) && rating.responses.length > 0
                                                                ? formatDate(rating.responses[0].responseDate)
                                                                : formatDate(rating.response?.responseDate)
                                                        }
                                                    </small>
                                                </div>
                                            ) : null}
                                        </Card.Body>
                                        <Card.Footer className="bg-white">
                                            <div className="d-grid">
                                                {!hasResponse(rating) ? (
                                                    // Nếu chưa có phản hồi, hiển thị nút phản hồi bình thường
                                                    <Button
                                                        as={Link}
                                                        to={`/doctor/response/${rating.id}`}
                                                        variant="danger"
                                                    >
                                                        <FaReply className="me-1" />
                                                        Phản hồi đánh giá
                                                    </Button>
                                                ) : isWithinEditWindow(rating) ? (
                                                    // Nếu có phản hồi và trong thời gian chỉnh sửa
                                                    <Button
                                                        as={Link}
                                                        to={`/doctor/response/${rating.id}`}
                                                        variant="outline-primary"
                                                    >
                                                        <FaReply className="me-1" />
                                                        Chỉnh sửa phản hồi
                                                    </Button>
                                                ) : (
                                                    // Nếu có phản hồi nhưng đã quá thời gian chỉnh sửa
                                                    <Button
                                                        variant="secondary"
                                                        disabled
                                                    >
                                                        <FaReply className="me-1" />
                                                        Hết thời gian chỉnh sửa
                                                    </Button>
                                                )}
                                            </div>
                                        </Card.Footer>
                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    )}
                </>
            )}
        </Container>
    );
};

export default DoctorRatings;