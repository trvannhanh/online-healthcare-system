import { useEffect, useState } from "react";
import { Button, Col, Container, Form, InputGroup, Nav, Navbar, NavDropdown, Row } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import { Link, useNavigate } from "react-router-dom";
import { FaSearch, FaHospital, FaStethoscope, FaUser } from "react-icons/fa"; // Thêm icon từ react-icons
import { useMyDispatcher, useMyUser } from "../../configs/MyContexts";
import cookie from 'react-cookies'

const Header = () => {
    const [hospitals, setHospitals] = useState([]);
    const [specialization, setSpecialization] = useState([]);
    const [doctorName, setDoctorName] = useState("");
    const nav = useNavigate();
    const { user } = useMyUser(); // Lấy thông tin người dùng từ context
    const dispatch = useMyDispatcher(); // Dùng để dispatch logout

    const loadHospitals = async () => {
        let res = await Apis.get(endpoints["hospitals"]);
        setHospitals(res.data);
    };

    const loadSpecialization = async () => {
        let res = await Apis.get(endpoints["specialization"]);
        setSpecialization(res.data);
    };

    const search = (e) => {
        e.preventDefault();
        nav(`/?doctorName=${doctorName}`);
    };

    const logout = () => {
        cookie.remove("token"); // Xóa token khỏi cookie
        dispatch({ type: "logout" }); // Cập nhật context
        nav("/login"); // Điều hướng về trang login
    };

    //  // Kiểm tra token và khôi phục trạng thái đăng nhập khi tải trang
    // useEffect(() => {
    //     const token = cookie.load("token");
    //     if (token && !user) {
    //         authApis()
    //             .get(endpoints["current-user"])
    //             .then((res) => {
    //                 dispatch({ type: "login", payload: res.data });
    //             })
    //             .catch((err) => {
    //                 console.error("Failed to fetch current user:", err);
    //                 cookie.remove("token"); // Xóa token nếu không hợp lệ
    //             });
    //     }
    // }, [user, dispatch]);


    useEffect(() => {
        loadHospitals();
        loadSpecialization();
    }, []);

    return (
        <Navbar expand="lg" sticky="top" className="shadow py-3" style={{ backgroundColor: '#ffffff', borderBottom: '2px solid #e3f2fd' }}>
            <Container>
                {/* Logo */}
                <Navbar.Brand as={Link} to="/" className="d-flex align-items-center">
                    <FaHospital size={36} className="me-2 text-primary" />
                    <span className="fw-bold" style={{ fontSize: "1.8rem", color: "#0d6efd", letterSpacing: '1px' }}>
                        Heal<span style={{ color: "#198754" }}>Care</span>
                    </span>
                </Navbar.Brand>

                <Navbar.Toggle />
                <Navbar.Collapse>
                    {/* Navigation links */}
                    <Nav className="me-auto ms-4">
                        <Link to="/" className="nav-link fs-6 text-dark fw-semibold">Trang Chủ</Link>
                        <NavDropdown title={<span><FaHospital className="me-1 text-info" />Bệnh Viện</span>} className="fw-semibold">
                            {hospitals.map(h => (
                                <NavDropdown.Item key={h.id} as={Link} to={`/?hospital=${h.name}`}>
                                    {h.name}
                                </NavDropdown.Item>
                            ))}
                        </NavDropdown>
                        <NavDropdown title={<span><FaStethoscope className="me-1 text-success" />Chuyên Khoa</span>} className="fw-semibold">
                            {specialization.map(s => (
                                <NavDropdown.Item key={s.id} as={Link} to={`/?specialization=${s.name}`}>
                                    {s.name}
                                </NavDropdown.Item>
                            ))}
                        </NavDropdown>
                    </Nav>

                    {/* Search bar */}
                    <Form onSubmit={search} className="d-flex me-3">
                        <InputGroup>
                            <Form.Control
                                type="text"
                                placeholder="Tìm bác sĩ..."
                                value={doctorName}
                                onChange={(e) => setDoctorName(e.target.value)}
                                className="border-primary"
                            />
                            <Button type="submit" variant="primary">
                                <FaSearch />
                            </Button>
                        </InputGroup>
                    </Form>

                    {/* User menu */}
                    {user ? (
                        <NavDropdown title={<span><FaUser className="me-1" />{user.firstName}</span>}>
                            <NavDropdown.Item onClick={logout}>Đăng xuất</NavDropdown.Item>
                        </NavDropdown>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-outline-primary me-2">Đăng Nhập</Link>
                            <Link to="/register" className="btn btn-primary">Đăng Ký</Link>
                        </>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Header;