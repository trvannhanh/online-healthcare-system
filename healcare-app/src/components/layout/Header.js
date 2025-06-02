
import { useEffect, useState, useCallback } from "react";
import { Button, Container, Form, InputGroup, Nav, Navbar, NavDropdown, Badge } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import { Link, useNavigate } from "react-router-dom";
import { FaHospital, FaUser, FaBell } from "react-icons/fa";
import { useMyDispatcher, useMyUser } from "../../configs/MyContexts";
import cookie from 'react-cookies';

const Header = () => {
    const nav = useNavigate();
    const { user } = useMyUser();
    const dispatch = useMyDispatcher();
    const [unreadNotifications, setUnreadNotifications] = useState(0);

    const logout = () => {
        cookie.remove("token");
        dispatch({ type: "logout" });
        nav("/login");
    };

    const checkVerified = (e) => {
        if (user && user.role === "DOCTOR" && !user.isVerified) {
            e.preventDefault();
            alert("Tài khoản của bạn chưa được xác nhận giấy phép hành nghề. Vui lòng chờ quản trị viên xác nhận.");
            return false;
        }
        return true;
    };


    const fetchNotificationsCount = useCallback(async () => {
        if (!user) return;

        try {
            const res = await authApis().get(endpoints["allNotifications"]);
            // Đếm những thông báo chưa đọc
            const unreadCount = res.data.filter(notif => !notif.read ).length;
            setUnreadNotifications(unreadCount);
        } catch (error) {
            console.error("Error fetching notifications:", error);
        }
    }, [user]); 

    useEffect(() => {
        fetchNotificationsCount();
    }, [user]);

    // Thêm useEffect mới để xử lý thông báo
    useEffect(() => {
        if (user && user.role === 'PATIENT') {
            // Gọi ngay khi component được tải
            fetchNotificationsCount();

            // Thiết lập lắng nghe sự kiện từ components khác
            const handleNotificationRead = () => {
                fetchNotificationsCount();
            };
            window.addEventListener('notification-read', handleNotificationRead);

            // Thiết lập interval để cập nhật thông báo định kỳ
            const intervalId = setInterval(fetchNotificationsCount, 60000); // Cập nhật mỗi phút

            // Cleanup khi component unmount
            return () => {
                window.removeEventListener('notification-read', handleNotificationRead);
                clearInterval(intervalId);
            };
        }
    }, [user, fetchNotificationsCount]); // Thêm fetchNotificationsCount vào dependencies

    return (
        <Navbar
            expand="lg"
            sticky="top"
            className="shadow-lg py-3"
            style={{
                background: 'linear-gradient(to right, #0d6efd, #20c997)',
                color: '#fff'
            }}
        >
            <Container>
                {/* Logo */}
                <Navbar.Brand as={Link} to="/" className="d-flex align-items-center">
                    <FaHospital
                        size={40}
                        className="me-2 text-white"
                        style={{ transition: 'transform 0.3s' }}
                        onMouseEnter={(e) => e.target.style.transform = 'scale(1.1)'}
                        onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                    />
                    <span
                        className="fw-bold"
                        style={{
                            fontSize: '2rem',
                            background: 'linear-gradient(to right, #fff, #e0f7fa)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent'
                        }}
                    >
                        Heal<span style={{ color: '#20c997' }}>Care</span>
                    </span>
                </Navbar.Brand>

                <Navbar.Toggle className="bg-white bg-opacity-25 border-0" />
                <Navbar.Collapse>
                    {/* Navigation links */}
                    <Nav className="me-auto ms-4">
                        <Link
                            to="/"
                            className="nav-link text-white fw-semibold px-3 py-2 rounded d-flex justify-content-center align-items-center"
                            style={{ transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = 'rgba(255,255,255,0.1)'}
                            onMouseLeave={(e) => e.target.style.background = 'transparent'}
                        >
                            Trang Chủ
                        </Link>

                        <Link 
                            to="/appointment" 
                            className="nav-link text-white fw-semibold px-3 py-2 rounded"
                            style={{ transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = 'rgba(255,255,255,0.1)'}
                            onMouseLeave={(e) => e.target.style.background = 'transparent'}
                            onClick={(e) => checkVerified(e)}
                        >
                            Lịch Hẹn
                        </Link>
                        {user && user.role === 'DOCTOR' && (
                            <Link to="/doctor/statistic"
                                className="nav-link text-white fw-semibold px-3 py-2 rounded d-flex justify-content-center align-items-center"
                                onClick={(e) => checkVerified(e)}
                            >
                                Thống kê bệnh nhân
                            </Link>
                        )}
                        {user && user.role === 'PATIENT' && (
                            <Link to="/pending-rating" className="nav-link text-white fw-semibold px-3 py-2 rounded d-flex justify-content-center align-items-center">Quản lý đánh giá</Link>
                        )}
                        {user && user.role === 'DOCTOR' && (
                            <Link to="/doctor/ratings" className="nav-link text-white fw-semibold px-3 py-2 rounded d-flex justify-content-center align-items-center">
                                Quản lý gửi phản hồi
                            </Link>
                        )}
                        {user && user.role === 'PATIENT' && (
                            <div className="mx-3 position-relative" style={{ cursor: 'pointer' }}>

                                <Link to="/notifications" className="text-decoration-none ">
                                    <FaBell
                                        size={22}
                                        className="text-white "
                                        style={{
                                            transition: 'transform 0.2s'
                                        }}
                                        onMouseEnter={(e) => e.target.style.transform = 'scale(1.2)'}
                                        onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                                    />
                                    {unreadNotifications > 0 && (
                                        <Badge
                                            pill
                                            bg="danger"
                                            className="position-absolute top-0 start-100 translate-middle "
                                            style={{
                                                fontSize: '0.6rem',
                                                padding: '0.25rem 0.4rem'
                                            }}
                                        >
                                            {unreadNotifications}
                                        </Badge>
                                    )}
                                </Link>
                            </div>
                        )}
                    </Nav>

                    {/* User menu */}
                    {user ? (
                        <NavDropdown
                            title={
                                <span className="text-white fw-semibold">
                                    <FaUser className="me-1" /> {user.firstName}
                                </span>
                            }
                            className="px-3 py-2 rounded"
                            style={{ transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = 'rgba(255,255,255,0.1)'}
                            onMouseLeave={(e) => e.target.style.background = 'transparent'}
                        >
                            <NavDropdown.Item as={Link} to="/profile" className="py-2 hover:bg-teal-100">
                                Thông Tin Cá Nhân
                            </NavDropdown.Item>
                            <NavDropdown.Item onClick={logout} className="py-2 hover:bg-teal-100">
                                Đăng Xuất
                            </NavDropdown.Item>
                        </NavDropdown>
                    ) : (
                        <>
                            <Link
                                to="/login"
                                className="btn btn-outline-light me-2 rounded-pill px-4"
                                style={{ borderColor: '#fff', color: '#fff' }}
                            >
                                Đăng Nhập
                            </Link>
                            <Link
                                to="/register"
                                className="btn btn-success rounded-pill px-4"
                                style={{ backgroundColor: '#20c997', borderColor: '#20c997' }}
                            >
                                Đăng Ký
                            </Link>
                        </>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Header;