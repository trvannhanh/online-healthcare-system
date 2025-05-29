import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Form, Container, Row, ProgressBar } from "react-bootstrap";
import Apis, { endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";

const Register = () => {
    const avatar = useRef();
    const nav = useNavigate();
    const [user, setUser] = useState({ role: "PATIENT" });
    const [errors, setErrors] = useState({});
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
                setErrors({ general: "Không thể tải dữ liệu bệnh viện hoặc chuyên khoa." });
            }
        };
        loadData();
    }, []);

    const setState = (value, field) => {
        setUser({ ...user, [field]: value });
        setErrors({ ...errors, [field]: null });
    };

    const validate = (currentStep) => {
        const newErrors = {};
        const usernameRegex = /^[a-zA-Z0-9_]{3,30}$/;
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,50}$/;
        const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
        const phoneRegex = /^\d{10}$/;
        const insuranceRegex = /^[A-Za-z0-9]{10,20}$/;
        const licenseRegex = /^[A-Za-z0-9]{8,20}$/;

        if (currentStep === 1) {
            if (!user.firstName || user.firstName.length > 50) {
                newErrors.firstName = "Họ không được để trống và tối đa 50 ký tự.";
            }
            if (!user.lastName || user.lastName.length > 50) {
                newErrors.lastName = "Tên không được để trống và tối đa 50 ký tự.";
            }
            if (!user.email || !emailRegex.test(user.email) || user.email.length > 50) {
                newErrors.email = "Email không hợp lệ hoặc vượt quá 50 ký tự.";
            }
            if (!user.username || !usernameRegex.test(user.username)) {
                newErrors.username = "Tên đăng nhập phải từ 3-30 ký tự, chỉ chứa chữ, số hoặc dấu gạch dưới.";
            }
            if (!user.password || !passwordRegex.test(user.password)) {
                newErrors.password = "Mật khẩu phải từ 8-50 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt.";
            }
            if (!user.confirm || user.password !== user.confirm) {
                newErrors.confirm = "Mật khẩu xác nhận không khớp.";
            }
            if (!user.phoneNumber || !phoneRegex.test(user.phoneNumber)) {
                newErrors.phoneNumber = "Số điện thoại phải là 10 chữ số.";
            }
        } else if (currentStep === 2 && user.role === "PATIENT") {
            if (!user.dateOfBirth) {
                newErrors.dateOfBirth = "Ngày sinh không được để trống.";
            } else {
                const dob = new Date(user.dateOfBirth);
                const today = new Date();
                if (dob > today) {
                    newErrors.dateOfBirth = "Ngày sinh không được là ngày trong tương lai.";
                }
            }
            if (!user.insuranceNumber || !insuranceRegex.test(user.insuranceNumber)) {
                newErrors.insuranceNumber = "Số bảo hiểm phải từ 10-20 ký tự, chỉ chứa chữ và số.";
            }
        } else if (currentStep === 2 && user.role === "DOCTOR") {
            if (!user.licenseNumber || !licenseRegex.test(user.licenseNumber)) {
                newErrors.licenseNumber = "Số giấy phép phải từ 8-20 ký tự, chỉ chứa chữ và số.";
            }
            if (!user.hospital) {
                newErrors.hospital = "Vui lòng chọn bệnh viện.";
            }
            if (!user.specialization) {
                newErrors.specialization = "Vui lòng chọn chuyên khoa.";
            }
        } else if (currentStep === 3) {
            if (!avatar.current || !avatar.current.files[0]) {
                newErrors.avatar = "Ảnh đại diện là bắt buộc.";
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const nextStep = () => {
        if (!validate(step)) return;
        setErrors({});
        setStep(step + 1);
    };

    const prevStep = () => setStep(step - 1);

    const register = async (e) => {
        e.preventDefault();
        if (!validate(3)) return;

        let form = new FormData();
        for (let key in user) {
            if (key !== "confirm") form.append(key, user[key]);
        }
        form.append("avatar", avatar.current.files[0]);

        try {
            setLoading(true);
            let res = await Apis.post(endpoints["register"], form, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            if (res.status === 201) {
                setErrors({});
                nav("/login");
            }
        } catch (err) {
            console.error(err);
            setErrors({ general: err.response?.data || "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin." });
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

                        {errors.general && <Alert variant="danger">{errors.general}</Alert>}

                        <Form onSubmit={register}>
                            {step === 1 && (
                                <>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Họ</Form.Label>
                                        <Form.Control
                                            value={user.firstName || ""}
                                            onChange={e => setState(e.target.value, "firstName")}
                                            required
                                            maxLength={50}
                                        />
                                        {errors.firstName && <Form.Text className="text-danger">{errors.firstName}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Tên</Form.Label>
                                        <Form.Control
                                            value={user.lastName || ""}
                                            onChange={e => setState(e.target.value, "lastName")}
                                            required
                                            maxLength={50}
                                        />
                                        {errors.lastName && <Form.Text className="text-danger">{errors.lastName}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Email</Form.Label>
                                        <Form.Control
                                            type="email"
                                            value={user.email || ""}
                                            onChange={e => setState(e.target.value, "email")}
                                            required
                                            maxLength={50}
                                        />
                                        {errors.email && <Form.Text className="text-danger">{errors.email}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Tên đăng nhập</Form.Label>
                                        <Form.Control
                                            value={user.username || ""}
                                            onChange={e => setState(e.target.value, "username")}
                                            required
                                            maxLength={30}
                                        />
                                        {errors.username && <Form.Text className="text-danger">{errors.username}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Mật khẩu</Form.Label>
                                        <Form.Control
                                            type="password"
                                            value={user.password || ""}
                                            onChange={e => setState(e.target.value, "password")}
                                            required
                                            maxLength={50}
                                        />
                                        {errors.password && <Form.Text className="text-danger">{errors.password}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Xác nhận mật khẩu</Form.Label>
                                        <Form.Control
                                            type="password"
                                            value={user.confirm || ""}
                                            onChange={e => setState(e.target.value, "confirm")}
                                            required
                                            maxLength={50}
                                        />
                                        {errors.confirm && <Form.Text className="text-danger">{errors.confirm}</Form.Text>}
                                    </Form.Group>
                                    <Form.Group className="mb-2">
                                        <Form.Label>Số điện thoại</Form.Label>
                                        <Form.Control
                                            value={user.phoneNumber || ""}
                                            onChange={e => setState(e.target.value, "phoneNumber")}
                                            required
                                            maxLength={10}
                                            placeholder="Nhập 10 chữ số"
                                        />
                                        {errors.phoneNumber && <Form.Text className="text-danger">{errors.phoneNumber}</Form.Text>}
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
                                                <Form.Control
                                                    value={user.insuranceNumber || ""}
                                                    onChange={e => setState(e.target.value, "insuranceNumber")}
                                                    maxLength={20}
                                                    required
                                                />
                                                {errors.insuranceNumber && <Form.Text className="text-danger">{errors.insuranceNumber}</Form.Text>}
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Ngày sinh</Form.Label>
                                                <Form.Control
                                                    type="date"
                                                    value={user.dateOfBirth || ""}
                                                    onChange={e => setState(e.target.value, "dateOfBirth")}
                                                    required
                                                />
                                                {errors.dateOfBirth && <Form.Text className="text-danger">{errors.dateOfBirth}</Form.Text>}
                                            </Form.Group>
                                        </>
                                    )}

                                    {user.role === "DOCTOR" && (
                                        <>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Số giấy phép</Form.Label>
                                                <Form.Control
                                                    value={user.licenseNumber || ""}
                                                    onChange={e => setState(e.target.value, "licenseNumber")}
                                                    maxLength={20}
                                                    required
                                                />
                                                {errors.licenseNumber && <Form.Text className="text-danger">{errors.licenseNumber}</Form.Text>}
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                                <Form.Label>Bệnh viện</Form.Label>
                                                <Form.Control
                                                    list="hospital-options"
                                                    value={user.hospital || ""}
                                                    onChange={e => setState(e.target.value, "hospital")}
                                                    required
                                                />
                                                <datalist id="hospital-options">
                                                    {hospitals.map(h => (
                                                        <option key={h.id} value={h.name} />
                                                    ))}
                                                </datalist>
                                                {errors.hospital && (
                                                    <Form.Text className="text-danger">{errors.hospital}</Form.Text>
                                                )}
                                            </Form.Group>
                                            <Form.Group className="mb-2">
                                            <Form.Label>Chuyên khoa</Form.Label>
                                            <Form.Control
                                                list="specialization-options"
                                                value={user.specialization || ""}
                                                onChange={e => setState(e.target.value, "specialization")}
                                                required
                                            />
                                            <datalist id="specialization-options">
                                                {specializations.map(s => (
                                                    <option key={s.id} value={s.name} />
                                                ))}
                                            </datalist>
                                            {errors.specialization && (
                                                <Form.Text className="text-danger">{errors.specialization}</Form.Text>
                                            )}
</Form.Group>
                                        </>
                                    )}
                                </>
                            )}

                            {step === 3 && (
                                <>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Ảnh đại diện</Form.Label>
                                        <Form.Control ref={avatar} type="file" accept="image/*" required />
                                        {errors.avatar && <Form.Text className="text-danger">{errors.avatar}</Form.Text>}
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