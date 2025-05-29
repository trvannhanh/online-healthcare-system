import { useState } from "react";
import { Alert, Button, Card, Col, Container, Form, Row } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";
import cookie from 'react-cookies';
import { useMyDispatcher } from "../configs/MyContexts";

const Login = () => {
    const info = [
        { title: "Tên đăng nhập", field: "username", type: "text" },
        { title: "Mật khẩu", field: "password", type: "password" }
    ];
    const [user, setUser] = useState({});
    const [msg, setMsg] = useState();
    const [loading, setLoading] = useState(false);
    const nav = useNavigate();
    const dispatch = useMyDispatcher();

    const setState = (value, field) => {
        setUser({ ...user, [field]: value });
    };

    const login = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            let res = await Apis.post(endpoints["login"], { ...user });
            cookie.save("token", res.data.token);

            // Lấy thông tin người dùng cơ bản
            let userRes = await authApis().get(endpoints["current-user"]);
            let baseUser = userRes.data;

            // Nếu là bác sĩ, lấy thông tin chi tiết bao gồm isVerified
            let fullUser = baseUser;
            if (baseUser.role === "DOCTOR") {
                const doctorRes = await authApis().get(`${endpoints["doctors"]}/${baseUser.id}`);
                fullUser = { ...baseUser, ...doctorRes.data, isVerified: doctorRes.data.isVerified };
            }

            dispatch({ type: "login", payload: fullUser });
            setMsg(null);
            nav("/");
        } catch (ex) {
            console.error(ex);
            setMsg("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            style={{
                background: "linear-gradient(rgba(13,110,253,0.8), rgba(13,110,253,0.8)), url('/images/hero-doctor.jpg') center/cover no-repeat",
                minHeight: "80vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
            }}
        >
            <Container>
                <Row className="justify-content-center">
                    <Col md={6} lg={5}>
                        <Card className="p-4 shadow-lg border-0 rounded-4">
                            <Card.Body>
                                <h3 className="text-center text-primary mb-4">ĐĂNG NHẬP</h3>

                                {msg && <Alert variant="danger">{msg}</Alert>}

                                <Form onSubmit={login}>
                                    {info.map(i => (
                                        <Form.Group className="mb-3" key={i.field}>
                                            <Form.Label>{i.title}</Form.Label>
                                            <Form.Control
                                                type={i.type}
                                                placeholder={i.title}
                                                value={user[i.field] || ""}
                                                onChange={e => setState(e.target.value, i.field)}
                                                required
                                            />
                                        </Form.Group>
                                    ))}

                                    {loading ? (
                                        <MySpinner />
                                    ) : (
                                        <Button
                                            type="submit"
                                            variant="primary"
                                            className="w-100 rounded-pill mt-2"
                                        >
                                            Đăng nhập
                                        </Button>
                                    )}
                                </Form>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};

export default Login;