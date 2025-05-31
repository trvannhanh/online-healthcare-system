import React, { useState, useEffect, useCallback } from 'react';
import { Toast, ToastContainer, Badge } from 'react-bootstrap';
import { FaBell, FaCalendarAlt, FaGift, FaHospital } from 'react-icons/fa';
import { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import { Link } from 'react-router-dom';

const PushNotification = () => {
    const [upcomingNotifications, setUpcomingNotifications] = useState([]);
    const [show, setShow] = useState(true);
    const { user } = useMyUser();

    const fetchUpcomingNotifications = useCallback(async () => {
        if (!user) return;

        try {
            const response = await authApis().get(endpoints['upcomingNotifications']);
            const notifications = response.data;
            setUpcomingNotifications(notifications.filter(n => !n.isRead));
        } catch (error) {
            console.error("Error fetching upcoming notifications:", error);
        }
    }, [user]);

    useEffect(() => {
        if (user) {
            fetchUpcomingNotifications();

            // Check for new notifications every minute
            const intervalId = setInterval(fetchUpcomingNotifications, 60000);
            return () => clearInterval(intervalId);
        }
    }, [fetchUpcomingNotifications, user]);

    // Get icon based on notification type
    const getIcon = (type) => {
        switch (type) {
            case 'APPOINTMENT': return <FaCalendarAlt className="me-2" />;
            case 'DISCOUNT': return <FaGift className="me-2" />;
            case 'HEALTH_PROGRAM': return <FaHospital className="me-2" />;
            default: return <FaBell className="me-2" />;
        }
    };

    // Mark notification as read
    const markAsRead = async (notificationId) => {
        try {
            await authApis().patch(endpoints['markNotificationAsRead'](notificationId));
            // Remove from local state
            setUpcomingNotifications(prevNotifications =>
                prevNotifications.filter(notification => notification.id !== notificationId)
            );
        } catch (error) {
            console.error("Error marking notification as read:", error);
        }
    };

    if (!user || upcomingNotifications.length === 0) return null;

    return (
        <div className="position-fixed" style={{ top: '80px', right: '20px', zIndex: 1050 }}>
            <ToastContainer>
                {upcomingNotifications.map((notification, index) => (
                    <Toast
                        key={notification.id}
                        onClose={() => markAsRead(notification.id)}
                        show={show}
                        delay={10000}
                        autohide
                        animation
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
                            {notification.message}
                            <div className="mt-2">
                                <Link
                                    to="/notifications"
                                    className="btn btn-sm btn-link ps-0"
                                    onClick={() => markAsRead(notification.id)}
                                >
                                    Xem tất cả thông báo
                                </Link>
                            </div>
                        </Toast.Body>
                    </Toast>
                ))}
            </ToastContainer>
        </div>
    );
};

export default PushNotification;