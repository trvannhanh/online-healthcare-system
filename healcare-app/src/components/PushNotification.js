import React, { useState, useEffect, useCallback } from 'react';
import { Toast, ToastContainer, Badge, Button } from 'react-bootstrap';
import { FaBell, FaCalendarAlt, FaGift, FaHospital, FaCheckCircle } from 'react-icons/fa';
import { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import { Link } from 'react-router-dom';

const PushNotification = () => {
    const [upcomingNotifications, setUpcomingNotifications] = useState([]);
    const [visibleNotifications, setVisibleNotifications] = useState({});
    const { user } = useMyUser();

    const fetchUpcomingNotifications = useCallback(async () => {
        if (!user) return;

        try {
            const response = await authApis().get(endpoints['upcomingNotifications']);
            const notifications = response.data;

            // Lấy thời gian hiện tại
            const now = new Date();

            // Lọc ra các thông báo:
            const validNotifications = notifications.filter(n => {
                // 1. Ưu tiên kiểm tra thời gian trước
                const sentDate = new Date(n.sentAt || n.createdAt);
                const now = new Date();

                // Chỉ lấy thông báo đã đến thời gian gửi
                if (sentDate > now) return false;

                // Tính thời gian đã trôi qua (giờ)
                const timeDiff = now - sentDate;
                const hoursPassed = timeDiff / (1000 * 60 * 60);

                // Loại bỏ thông báo quá cũ (> 24 giờ)
                if (hoursPassed > 24) return false;

                // 2. Sau đó mới kiểm tra trạng thái đọc
                return !n.isRead;
            });
            // Cập nhật state thông báo
            setUpcomingNotifications(validNotifications);

            // Đặt tất cả thông báo mới là hiển thị (visible)
            const newVisibleState = {};
            validNotifications.forEach(n => {
                newVisibleState[n.id] = true;
            });
            setVisibleNotifications(prev => ({ ...prev, ...newVisibleState }));
        } catch (error) {
            console.error("Error fetching upcoming notifications:", error);
        }
    }, [user]);

    useEffect(() => {
        if (user) {
            fetchUpcomingNotifications();
            const intervalId = setInterval(fetchUpcomingNotifications, 60000);
            return () => clearInterval(intervalId);
        }
    }, [fetchUpcomingNotifications, user]);

    // Thêm icon cho mỗi loại thông báo
    const getIcon = (type) => {
        switch (type) {
            case 'APPOINTMENT': return <FaCalendarAlt className="me-2" />;
            case 'DISCOUNT': return <FaGift className="me-2" />;
            case 'HEALTH_PROGRAM': return <FaHospital className="me-2" />;
            default: return <FaBell className="me-2" />;
        }
    };

    // Đánh dấu thông báo đã được đọc
    const markAsRead = async (notificationId) => {
        try {
            await authApis().patch(`/secure/notifications/${notificationId}/mark-read`);

            // Cập nhật state để loại bỏ thông báo đã đọc
            setUpcomingNotifications(prev =>
                prev.filter(notification => notification.id !== notificationId)
            );

            // Kích hoạt sự kiện cập nhật số lượng thông báo ở Header
            window.dispatchEvent(new Event('notification-read'));
        } catch (error) {
            console.error("Error marking notification as read:", error);
        }
    };

    // Chỉ ẩn thông báo tạm thời mà không đánh dấu là đã đọc
    const hideNotification = (notificationId) => {
        setVisibleNotifications(prev => ({
            ...prev,
            [notificationId]: false
        }));
    };

    if (!user) return null;

    return (
        <ToastContainer
            className="position-fixed p-3"
            position="bottom-end"
            style={{ zIndex: 1050 }}
        >
            {upcomingNotifications.map((notification) => (
                visibleNotifications[notification.id] && (
                    <Toast
                        key={notification.id}
                        onClose={() => hideNotification(notification.id)}
                        delay={10000}
                        autohide
                        animation
                        className="mb-2"
                    >
                        <Toast.Header>
                            {getIcon(notification.type)}
                            <strong className="me-auto">
                                {notification.type === 'APPOINTMENT' ? 'Lịch hẹn sắp tới' :
                                    notification.type === 'DISCOUNT' ? 'Ưu đãi đặc biệt' :
                                        notification.type === 'HEALTH_PROGRAM' ? 'Chương trình sức khỏe' :
                                            'Thông báo'}
                            </strong>
                            <small>{new Date(notification.sentAt || notification.createdAt).toLocaleDateString('vi-VN')}</small>
                        </Toast.Header>
                        <Toast.Body>
                            <p>{notification.message}</p>
                            <div className="d-flex justify-content-between mt-2">
                                <Button
                                    variant="outline-primary"
                                    size="sm"
                                    onClick={() => markAsRead(notification.id)}
                                >
                                    <FaCheckCircle className="me-1" /> Đánh dấu đã đọc
                                </Button>
                                <Link
                                    to="/notifications"
                                    className="btn btn-sm btn-link"
                                >
                                    Xem tất cả
                                </Link>
                            </div>
                        </Toast.Body>
                    </Toast>
                )
            ))}
        </ToastContainer>
    );
};

export default PushNotification;