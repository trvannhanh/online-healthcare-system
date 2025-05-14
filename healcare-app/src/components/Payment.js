import React, { useState, useEffect } from 'react';
import { Button, Container, Form, Alert, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';

const Payment = () => {
    const { appointmentId } = useParams(); // Lấy appointmentId từ URL, ví dụ: /payment/25
    const navigate = useNavigate();
    const { user } = useMyUser() || {};
    const [payment, setPayment] = useState(null);
    const [amount, setAmount] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('Momo');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const loadPayment = async () => {
        try {
            const res = await Apis.get(`${endpoints['payment']}/appointment/${appointmentId}`);
            setPayment(res.data);
        } catch (ex) {
            if (ex.response?.status === 404) {
                setPayment(null);
            } else {
                console.error('Load payment error:', ex);
                setError('Không thể tải thông tin hóa đơn. Vui lòng thử lại.');
            }
        }
    };

    useEffect(() => {
        if (appointmentId) loadPayment();
    }, [appointmentId]);

    // Tạo hóa đơn
    const handleCreatePayment = async (e) => {
        e.preventDefault();
        if (!amount || amount <= 0) {
            setError('Vui lòng nhập số tiền hợp lệ.');
            return;
        }

        setLoading(true);
        try {
            const res = await authApis().post(`${endpoints['createPayment'](appointmentId)}?amount=${amount}`);
            setPayment(res.data);
            setSuccess('Tạo hóa đơn thành công!');
            setTimeout(() => setSuccess(null), 3000);
        } catch (ex) {
            setError(ex.response?.data || 'Tạo hóa đơn thất bại. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    // Xử lý thanh toán
    const handleProcessPayment = async () => {
        if (!payment) {
            setError('Hóa đơn chưa được tạo.');
            return;
        }

        setLoading(true);
        try {
            const res = await authApis().post(`${endpoints['processPayment'](payment.id)}?paymentMethod=${paymentMethod}`);
            const paymentUrl = res.data;
            window.location.href = paymentUrl; // Chuyển hướng đến cổng thanh toán
        } catch (ex) {
            setError(ex.response?.data || 'Xử lý thanh toán thất bại. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    // Xử lý callback từ cổng thanh toán
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('orderId')) {
            setLoading(true);
            Apis.get(`${endpoints['paymentReturn']}?${urlParams.toString()}`)
                .then(res => {
                    setSuccess(res.data);
                    setTimeout(() => navigate('/'), 3000); // Quay về trang chủ sau 3 giây
                })
                .catch(ex => setError(ex.response?.data || 'Xử lý callback thất bại.'))
                .finally(() => setLoading(false));
        }
    }, [navigate]);

    if (!user) {
        return (
            <Container className="my-5">
                <Alert variant="warning">Vui lòng đăng nhập để thực hiện thanh toán!</Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h3>Thanh toán - Lịch hẹn #{appointmentId}</h3>
            {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
            {success && <Alert variant="success" dismissible onClose={() => setSuccess(null)}>{success}</Alert>}
            {user.role === 'DOCTOR' && !payment && (
                <Form onSubmit={handleCreatePayment}>
                    <Form.Group className="mb-3">
                        <Form.Label>Số tiền (VND)</Form.Label>
                        <Form.Control
                            type="number"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            placeholder="Nhập số tiền"
                            required
                        />
                    </Form.Group>
                    <Button variant="primary" type="submit" disabled={loading}>
                        {loading ? <Spinner animation="border" size="sm" /> : 'Tạo hóa đơn'}
                    </Button>
                </Form>
            )}
            {user.role === 'DOCTOR' && payment && (
                <div>
                    <p><strong>Đã tạo hóa đơn:</strong></p>
                    <p><strong>Số tiền:</strong> {payment.amount} VND</p>
                    <p><strong>Trạng thái:</strong> {payment.paymentStatus}</p>
                </div>
            )}

            {user.role === 'PATIENT' && !payment && (
                <Alert variant="info">Hóa đơn chưa được tạo. Vui lòng chờ bác sĩ tạo hóa đơn.</Alert>
            )}

            {user.role === 'PATIENT' && payment && (
                <>
                    <div className="mb-3">
                        <p><strong>Số tiền:</strong> {payment.amount} VND</p>
                        <p><strong>Trạng thái:</strong> {payment.paymentStatus}</p>
                    </div>
                    {payment.paymentStatus === 'PENDING' && (
                        <>
                            <Form.Group className="mb-3">
                                <Form.Label>Chọn phương thức thanh toán</Form.Label>
                                <Form.Select
                                    value={paymentMethod}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                >
                                    <option value="Momo">MoMo</option>
                                    <option value="VNPay">VNPay</option>
                                </Form.Select>
                            </Form.Group>
                            <Button variant="success" onClick={handleProcessPayment} disabled={loading}>
                                {loading ? <Spinner animation="border" size="sm" /> : 'Thanh toán'}
                            </Button>
                        </>
                    )}
                </>
            )}
        </Container>
    );
};

export default Payment;