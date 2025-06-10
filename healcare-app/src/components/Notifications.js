import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Badge, ListGroup, Button, Spinner, Alert } from 'react-bootstrap';
import { useMyUser } from '../configs/MyContexts';
import { authApis, endpoints } from '../configs/Apis';
import { FaBell, FaCheckCircle, FaEnvelope, FaCalendarAlt, FaGift, FaHospital } from 'react-icons/fa';
import { Link, useNavigate } from 'react-router-dom';

const Notifications = () => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { user } = useMyUser();
    const navigate = useNavigate();

    const loadAllNotifications = useCallback(async () => {
        if (!user) return;

        try {
            setLoading(true);
            const res = await authApis().get(endpoints['allNotifications']);
            setNotifications(res.data);
        } catch (ex) {
            console.error("Error loading notifications:", ex);
            setError("Không thể tải thông báo. Vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => {
        if (user) {
            loadAllNotifications();
        } else {
            navigate('/login');
        }
    }, [user, loadAllNotifications, navigate]);


    const markAsRead = async (notificationId) => {
        try {
            await authApis().patch(`/secure/notifications/${notificationId}/mark-read`);

            // Cập nhật thông báo
            setNotifications(prevNotifications =>
                prevNotifications.map(notification =>
                    notification.id === notificationId
                        ? { ...notification, isRead: true }
                        : notification
                )
            );
        } catch (ex) {
            console.error("Error marking notification as read:", ex);
            setError("Không thể đánh dấu thông báo là đã đọc");
        }
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'APPOINTMENT':
                return <FaCalendarAlt className="text-primary" />;
            case 'DISCOUNT':
                return <FaGift className="text-success" />;
            case 'HEALTH_PROGRAM':
                return <FaHospital className="text-info" />;
            default:
                return <FaEnvelope className="text-secondary" />;
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const unreadCount = notifications.filter(notification => !notification.isRead).length;

    if (!user) {
        return (
            <Container className="my-5">
                <Alert variant="warning">
                    Vui lòng đăng nhập để xem thông báo
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <Row className="mb-4">
                <Col>
                    <h2 className="text-primary">
                        <FaBell className="me-2" /> Thông báo của bạn
                        {unreadCount > 0 && (
                            <Badge bg="danger" pill className="ms-2">
                                {unreadCount} mới
                            </Badge>
                        )}
                    </h2>
                </Col>
            </Row>

            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible>
                    {error}
                </Alert>
            )}

            {loading ? (
                <div className="text-center my-5">
                    <Spinner animation="border" variant="primary" />
                    <p className="mt-3">Đang tải thông báo...</p>
                </div>
            ) : notifications.length === 0 ? (
                <Alert variant="info">
                    Bạn không có thông báo nào.
                </Alert>
            ) : (
                <Card className="shadow-sm">
                    <ListGroup variant="flush">
                        {notifications.map((notification) => (
                            <ListGroup.Item
                                key={notification.id}
                                className={`py-3 ${!notification.isRead ? 'bg-light' : ''}`}
                                style={{
                                    borderLeft: !notification.isRead ? '4px solid #0d6efd' : 'none',
                                    transition: 'all 0.3s ease'
                                }}
                            >
                                <Row className="align-items-center">
                                    <Col xs={1} className="text-center">
                                        {getNotificationIcon(notification.type)}
                                    </Col>
                                    <Col>
                                        <div className="d-flex justify-content-between">
                                            <h5 className="mb-1">
                                                {notification.type === 'APPOINTMENT' ? 'Lịch hẹn' :
                                                    notification.type === 'DISCOUNT' ? 'Ưu đãi' :
                                                        notification.type === 'HEALTH_PROGRAM' ? 'Chương trình sức khỏe' :
                                                            'Thông báo'}
                                            </h5>
                                            <small className="text-muted">
                                                {formatDate(notification.sentAt || notification.createdAt)}
                                            </small>
                                        </div>
                                        <p className="mb-1">{notification.message}</p>
                                        {!notification.read && (
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="mt-2"
                                                onClick={() => markAsRead(notification.id)}
                                            >
                                                <FaCheckCircle className="me-1" /> Đánh dấu đã đọc
                                            </Button>
                                        )}
                                    </Col>
                                </Row>
                            </ListGroup.Item>
                        ))}
                    </ListGroup>
                </Card>
            )}
        </Container>
    );
};

export default Notifications;