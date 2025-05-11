import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Form, Container, Row, ProgressBar } from "react-bootstrap";
import Apis, { endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";

const Register = () => {
    const avatar = useRef();
    const nav = useNavigate();
    const [user, setUser] = useState({ role: "PATIENT" });
    const [msg, setMsg] = useState();
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState(1);

    const [hospitals, setHospitals] = useState([]);
    const [specializations, setSpecializations] = useState([]);

    useEffect(() => {
        const loadData = async () => {
            try {
                const resHospitals = await Apis.get(endpoints["hospitals"]);
                setHospitals(resHospitals.data);

                const resSpecial = await Apis.get(endpoints["specialization"]);
                setSpecializations(resSpecial.data);
            } catch (err) {
                console.error(err);
            }
        };
        loadData();
    }, []);

    const setState = (value, field) => setUser({ ...user, [field]: value });

    const validate = () => {
        if (!user.password || user.password !== user.confirm) {
            setMsg("Mật khẩu không khớp!");
            return false;
        }
        if (user.role === "PATIENT" && (!user.insuranceNumber || !user.dateOfBirth)) {
            setMsg("Vui lòng nhập đầy đủ Số Bảo Hiểm và Ngày Sinh!");
            return false;
        }
        if (user.role === "DOCTOR" && (!user.licenseNumber || !user.hospital || !user.specialization)) {
            setMsg("Vui lòng nhập đầy đủ thông tin bác sĩ!");
            return false;
        }
        return true;
    };

    const nextStep = () => {
        if (step === 1 && (!user.firstName || !user.lastName || !user.email || !user.username || !user.password || !user.confirm)) {
            setMsg("Vui lòng điền đầy đủ thông tin cơ bản.");
            return;
        }
        if (step === 2 && !validate()) return;
        setMsg(null);
        setStep(step + 1);
    };

    const prevStep = () => setStep(step - 1);

    const register = async (e) => {
        e.preventDefault();
        let form = new FormData();
        for (let key in user)
            if (key !== "confirm") form.append(key, user[key]);
        if (avatar.current && avatar.current.files[0]) {
            form.append("avatar", avatar.current.files[0]);
        }
        try {
            setLoading(true);
            let res = await Apis.post(endpoints["register"], form, {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            });
            if (res.status === 201) nav("/login");
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const progress = step * 33;

    return (
        <Container className="mt-5">
            <Row className="justify-content-center">
                <Col md={8} lg={6}>
                    <div className="shadow rounded p-4 bg-white">
                        <h4 className="text-center text-primary mb-3">ĐĂNG KÝ</h4>
                        <ProgressBar now={progress} label={`Bước ${step}/3`} className="mb-3" />

                        {msg && <Alert variant="danger">{msg}</Alert>}

                        <Form onSubmit={register}>
                            {step === 1 && (
                                <>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Họ</Form.Label>
                                        <Form.Control value={user.firstName || ""} onChange={e => setState(e.target.value, "firstName")} required />
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Tên</Form.Label>
                                        <Form.Control value={user.lastName || ""} onChange={e => setState(e.target.value, "lastName")} required />
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Email</Form.Label>
                                        <Form.Control type="email" value={user.email || ""} onChange={e => setState(e.target.value, "email")} required />
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Tên đăng nhập</Form.Label>
                                        <Form.Control value={user.username || ""} onChange={e => setState(e.target.value, "username")} required />
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Mật khẩu</Form.Label>
                                        <Form.Control type="password" value={user.password || ""} onChange={e => setState(e.target.value, "password")} required />
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Xác nhận mật khẩu</Form.Label>
                                        <Form.Control type="password" value={user.confirm || ""} onChange={e => setState(e.target.value, "confirm")} required />
                                    </Form.Group>
                                </>
                            )}

                            {step === 2 && (
                                <>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Vai trò</Form.Label>
                                        <Form.Select value={user.role} onChange={e => setState(e.target.value, "role")}>
                                            <option value="PATIENT">Bệnh nhân</option>
                                            <option value="DOCTOR">Bác sĩ</option>
                                        </Form.Select>
                                    </Form.Group>

                                    {user.role === "PATIENT" && (
                                        <>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Số Bảo Hiểm</Form.Label>
                                                <Form.Control value={user.insuranceNumber || ""} onChange={e => setState(e.target.value, "insuranceNumber")} />
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Ngày sinh</Form.Label>
                                                <Form.Control type="date" value={user.dateOfBirth || ""} onChange={e => setState(e.target.value, "dateOfBirth")} />
                                            </Form.Group>
                                        </>
                                    )}

                                    {user.role === "DOCTOR" && (
                                        <>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Số giấy phép</Form.Label>
                                                <Form.Control value={user.licenseNumber || ""} onChange={e => setState(e.target.value, "licenseNumber")} />
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Bệnh viện</Form.Label>
                                                <Form.Select value={user.hospital || ""} onChange={e => setState(e.target.value, "hospital")}>
                                                    <option value="">-- Chọn --</option>
                                                    {hospitals.map(h => <option key={h.id} value={h.name}>{h.name}</option>)}
                                                </Form.Select>
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Chuyên khoa</Form.Label>
                                                <Form.Select value={user.specialization || ""} onChange={e => setState(e.target.value, "specialization")}>
                                                    <option value="">-- Chọn --</option>
                                                    {specializations.map(s => <option key={s.id} value={s.name}>{s.name}</option>)}
                                                </Form.Select>
                                            </Form.Group>
                                        </>
                                    )}
                                </>
                            )}

                            {step === 3 && (
                                <>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Ảnh đại diện</Form.Label>
                                        <Form.Control ref={avatar} type="file" required />
                                    </Form.Group>
                                </>
                            )}

                            <div className="d-flex justify-content-between mt-3">
                                {step > 1 && (
                                    <Button variant="secondary" onClick={prevStep}>
                                        Quay lại
                                    </Button>
                                )}
                                {step < 3 && (
                                    <Button variant="primary" onClick={nextStep}>
                                        Tiếp tục
                                    </Button>
                                )}
                                {step === 3 && (
                                    loading ? <MySpinner /> : <Button type="submit" variant="success">Đăng ký</Button>
                                )}
                            </div>
                        </Form>
                    </div>
                </Col>
            </Row>
        </Container>
    );
};

export default Register;