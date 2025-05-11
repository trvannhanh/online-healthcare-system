import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Form, Container, Row } from "react-bootstrap";
import Apis, { endpoints} from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";

const Register = () => {
    const info = [{
        label: "Họ",
        field: "firstName",
        type: "text"
    }, {
        label: "Tên",
        field: "lastName",
        type: "text"
    }, {
        label: "Email",
        field: "email",
        type: "email"
    }, {
        label: "Điện thoại",
        field: "phoneNumber",
        type: "tel"
    }, {
        label: "Tên đăng nhập",
        field: "username",
        type: "text"
    }, {
        label: "Mật khẩu",
        field: "password",
        type: "password"
    }, {
        label: "Xác nhận mật khẩu",
        field: "confirm",
        type: "password"
    }];

    const avatar = useRef();

    const [user, setUser] = useState({role: "PATIENT"});
    const [msg, setMsg] = useState();
    const [loading, setLoading] = useState(false);
    const [hospitals, setHospitals] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const nav = useNavigate();

    const loadHospitals = async () => {
        try {
            let res = await Apis.get(endpoints["hospitals"]);
            setHospitals(res.data);
        } catch (ex) {
            console.error(ex);
        }
    };

    const loadSpecializations = async () => {
        try {
            let res = await Apis.get(endpoints["specialization"]);
            setSpecializations(res.data);
        } catch (ex) {
            console.error(ex);
        }
    };

    useEffect(() => {
        loadHospitals();
        loadSpecializations();
    }, []);

    const validate = () => {
        if (!user.password || user.password !== user.confirm) {
            setMsg("Mật khẩu không khớp!");
            return false;
        }

        // Kiểm tra các trường bắt buộc dựa trên role
        if (user.role === "PATIENT") {
            if (!user.insuranceNumber || !user.dateOfBirth) {
                setMsg("Vui lòng nhập đầy đủ Số Bảo Hiểm và Ngày Sinh!");
                return false;
            }
        } else if (user.role === "DOCTOR") {
            if (!user.licenseNumber || !user.hospital || !user.specialization) {
                setMsg("Vui lòng nhập đầy đủ Số Giấy Phép, Bệnh Viện và Chuyên Khoa!");
                return false;
            }
        }

        return true;
    }

    const register = async (e) => {
        e.preventDefault();

        if (validate()) {
            let form = new FormData();
            for (let key in user) 
                if (key !== 'confirm') {
                    form.append(key, user[key]);
                }

            if (avatar) {
                form.append('avatar', avatar.current.files[0]);
            }

            try {
                setLoading(true);
                let res = await Apis.post(endpoints['register'], form, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                });

                if (res.status === 201)
                    nav('/login');
            } catch (ex) {
                console.error(ex);
            } finally {
                setLoading(false);
            }
        }
    }



    return (
        <div className="bg-light min-vh-100 d-flex align-items-center justify-content-center">
            <Container>
                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <Form
                            onSubmit={register}
                            className="p-3 rounded shadow-sm bg-white"
                        >
                            <h2 className="text-center mb-3 text-primary fw-bold fs-4">
                                ĐĂNG KÝ NGƯỜI DÙNG
                            </h2>

                            {msg && (
                                <Alert
                                    variant="danger"
                                    className="mb-3 text-center py-2"
                                >
                                    {msg}
                                </Alert>
                            )}

                            <Row>
                                {/* Các trường cơ bản */}
                                {info.map((i, index) => (
                                    <Col md={6} key={i.field}>
                                        <Form.Group className="mb-2" controlId={i.field}>
                                            <Form.Label className="text-dark fw-medium small">
                                                {i.label}
                                            </Form.Label>
                                            <Form.Control
                                                size="sm"
                                                value={user[i.field] || ""}
                                                onChange={(e) =>
                                                    setUser({ ...user, [i.field]: e.target.value })
                                                }
                                                type={i.type}
                                                placeholder={i.label}
                                                required
                                                className="border-secondary"
                                            />
                                        </Form.Group>
                                    </Col>
                                ))}

                                {/* Trường chọn Role */}
                                <Col md={6}>
                                    <Form.Group className="mb-2" controlId="role">
                                        <Form.Label className="text-dark fw-medium small">
                                            Vai Trò
                                        </Form.Label>
                                        <Form.Select
                                            size="sm"
                                            value={user.role}
                                            onChange={(e) =>
                                                setUser({ ...user, role: e.target.value })
                                            }
                                            className="border-secondary"
                                        >
                                            <option value="PATIENT">Bệnh Nhân</option>
                                            <option value="DOCTOR">Bác sĩ</option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>

                                {/* Các trường bổ sung cho Bệnh Nhân */}
                                {user.role === "PATIENT" && (
                                    <>
                                        <Col md={6}>
                                            <Form.Group className="mb-2" controlId="insuranceNumber">
                                                <Form.Label className="text-dark fw-medium small">
                                                    Số Bảo Hiểm
                                                </Form.Label>
                                                <Form.Control
                                                    size="sm"
                                                    value={user.insuranceNumber || ""}
                                                    onChange={(e) =>
                                                        setUser({
                                                            ...user,
                                                            insuranceNumber: e.target.value,
                                                        })
                                                    }
                                                    type="text"
                                                    placeholder="Số Bảo Hiểm"
                                                    required
                                                    className="border-secondary"
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-2" controlId="dateOfBirth">
                                                <Form.Label className="text-dark fw-medium small">
                                                    Ngày Sinh
                                                </Form.Label>
                                                <Form.Control
                                                    size="sm"
                                                    value={user.dateOfBirth || ""}
                                                    onChange={(e) =>
                                                        setUser({
                                                            ...user,
                                                            dateOfBirth: e.target.value,
                                                        })
                                                    }
                                                    type="date"
                                                    required
                                                    className="border-secondary"
                                                />
                                            </Form.Group>
                                        </Col>
                                    </>
                                )}

                                {/* Các trường bổ sung cho Bác sĩ */}
                                {user.role === "DOCTOR" && (
                                    <>
                                        <Col md={6}>
                                            <Form.Group className="mb-2" controlId="licenseNumber">
                                                <Form.Label className="text-dark fw-medium small">
                                                    Số Giấy Phép
                                                </Form.Label>
                                                <Form.Control
                                                    size="sm"
                                                    value={user.licenseNumber || ""}
                                                    onChange={(e) =>
                                                        setUser({
                                                            ...user,
                                                            licenseNumber: e.target.value,
                                                        })
                                                    }
                                                    type="text"
                                                    placeholder="Số Giấy Phép"
                                                    required
                                                    className="border-secondary"
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-2" controlId="hospital">
                                                <Form.Label className="text-dark fw-medium small">
                                                    Bệnh Viện Công Tác
                                                </Form.Label>
                                                <Form.Select
                                                    size="sm"
                                                    value={user.hospital || ""}
                                                    onChange={(e) =>
                                                        setUser({
                                                            ...user,
                                                            hospital: e.target.value,
                                                        })
                                                    }
                                                    required
                                                    className="border-secondary"
                                                >
                                                    <option value="">Chọn Bệnh Viện</option>
                                                    {hospitals.map((h) => (
                                                        <option key={h.id} value={h.name}>
                                                            {h.name}
                                                        </option>
                                                    ))}
                                                </Form.Select>
                                            </Form.Group>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Group className="mb-2" controlId="specialization">
                                                <Form.Label className="text-dark fw-medium small">
                                                    Chuyên Khoa
                                                </Form.Label>
                                                <Form.Select
                                                    size="sm"
                                                    value={user.specialization || ""}
                                                    onChange={(e) =>
                                                        setUser({
                                                            ...user,
                                                            specialization: e.target.value,
                                                        })
                                                    }
                                                    required
                                                    className="border-secondary"
                                                >
                                                    <option value="">Chọn Chuyên Khoa</option>
                                                    {specializations.map((s) => (
                                                        <option key={s.id} value={s.name}>
                                                            {s.name}
                                                        </option>
                                                    ))}
                                                </Form.Select>
                                            </Form.Group>
                                        </Col>
                                    </>
                                )}

                                {/* Trường upload avatar */}
                                <Col md={6}>
                                    <Form.Group className="mb-2" controlId="avatar">
                                        <Form.Label className="text-dark fw-medium small">
                                            Ảnh Đại Diện
                                        </Form.Label>
                                        <Form.Control
                                            size="sm"
                                            ref={avatar}
                                            type="file"
                                            required
                                            className="border-secondary"
                                        />
                                    </Form.Group>
                                </Col>
                            </Row>

                            {/* Nút Đăng ký */}
                            <div className="text-center mt-3">
                                {loading ? (
                                    <MySpinner />
                                ) : (
                                    <Button
                                        type="submit"
                                        variant="primary"
                                        className="w-50"
                                    >
                                        Đăng Ký
                                    </Button>
                                )}
                            </div>
                        </Form>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}

export default Register;