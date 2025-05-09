import { useEffect, useState } from "react";
import { Button, Col, Container, Form, InputGroup, Nav, Navbar, NavDropdown, Row } from "react-bootstrap";
import Apis, { endpoints } from "../../configs/Apis";
import { Link, useNavigate } from "react-router-dom";
import { FaSearch, FaHospital, FaStethoscope } from "react-icons/fa"; // Thêm icon từ react-icons

const Header = () => {
    const [hospitals, setHospitals] = useState([]);
    const [specialization, setSpecialization] = useState([]);
    const [doctorName, setDoctorName] = useState("");
    const nav = useNavigate();

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

    useEffect(() => {
        loadHospitals();
        loadSpecialization();
    }, []);

    return (
        <Navbar
            expand="lg"
            bg="light"
            variant="light"
            sticky="top"
            className="shadow-sm"
            style={{ backgroundColor: "#f8f9fa" }}
        >
            <Container>
                {/* Logo và Brand */}
                <Navbar.Brand as={Link} to="/" className="d-flex align-items-center">
                    <FaHospital size={30} className="me-2" style={{ color: "#007bff" }} />
                    <span className="fw-bold" style={{ color: "#007bff", fontSize: "1.5rem" }}>
                        HealCare
                    </span>
                </Navbar.Brand>

                <Navbar.Toggle aria-controls="basic-navbar-nav" />

                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto align-items-center">
                        <Link
                            to="/"
                            className="nav-link fw-medium mx-2"
                            style={{ color: "#343a40" }}
                        >
                            Trang Chủ
                        </Link>

                        {/* Dropdown Bệnh Viện */}
                        <NavDropdown
                            title={
                                <span>
                                    <FaHospital className="me-1" style={{ color: "#007bff" }} />
                                    Bệnh Viện
                                </span>
                            }
                            id="hospital-nav-dropdown"
                            className="mx-2"
                            renderMenuOnMount={true}
                        >
                            {hospitals.map((h) => (
                                <NavDropdown.Item
                                    as={Link}
                                    to={`/?hospital=${h.name}`}
                                    key={h.id}
                                    className="py-2"
                                >
                                    {h.name}
                                </NavDropdown.Item>
                            ))}
                        </NavDropdown>

                        {/* Dropdown Chuyên Khoa */}
                        <NavDropdown
                            title={
                                <span>
                                    <FaStethoscope className="me-1" style={{ color: "#007bff" }} />
                                    Chuyên Khoa
                                </span>
                            }
                            id="specialization-nav-dropdown"
                            className="mx-2"
                            renderMenuOnMount={true}
                        >
                            {specialization.map((s) => (
                                <NavDropdown.Item
                                    as={Link}
                                    to={`/?specialization=${s.name}`}
                                    key={s.id}
                                    className="py-2"
                                >
                                    {s.name}
                                </NavDropdown.Item>
                            ))}
                        </NavDropdown>

                        <Link
                            to="/login"
                            className="nav-link fw-medium mx-2"
                            style={{ color: "#343a40" }}
                        >
                            Đăng Nhập
                        </Link>

                        <Link
                            to="/register"
                            className="nav-link fw-medium mx-2"
                            style={{ color: "#343a40" }}
                        >
                            Đăng ký
                        </Link>
                    </Nav>

                    {/* Form Tìm kiếm */}
                    <Form onSubmit={search} className="d-flex align-items-center">
                        <InputGroup style={{ maxWidth: "300px" }}>
                            <Form.Control
                                type="text"
                                value={doctorName}
                                onChange={(e) => setDoctorName(e.target.value)}
                                placeholder="Tìm bác sĩ..."
                                className="rounded-start"
                                style={{ borderColor: "#007bff" }}
                            />
                            <Button
                                type="submit"
                                variant="primary"
                                className="rounded-end"
                                style={{ backgroundColor: "#007bff", borderColor: "#007bff" }}
                            >
                                <FaSearch />
                            </Button>
                        </InputGroup>
                    </Form>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Header;