import React, { useState, useEffect } from 'react';
import { Button, Container, Form, Alert, Spinner, Card } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';

const Payment = () => {
    const { appointmentId } = useParams();
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
            window.location.href = paymentUrl;
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
                    setTimeout(() => navigate('/'), 3000);
                })
                .catch(ex => setError(ex.response?.data || 'Xử lý callback thất bại.'))
                .finally(() => setLoading(false));
        }
    }, [navigate]);

    if (!user) {
        return (
            <Container className="my-5">
                <Alert 
                    variant="warning" 
                    className="shadow-sm rounded-pill px-4 py-3"
                >
                    Vui lòng đăng nhập để thực hiện thanh toán!{' '}
                    <a href="/login" className="ms-2 text-decoration-none fw-semibold">
                        Đăng Nhập
                    </a>
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-5">
            <h3 
                className="fw-bold mb-4" 
                style={{ 
                    color: '#0d6efd', 
                    fontSize: '2.2rem', 
                    textShadow: '1px 1px 2px rgba(0,0,0,0.1)' 
                }}
            >
                Thanh Toán - Lịch Hẹn #{appointmentId}
            </h3>

            {error && (
                <Alert 
                    variant="danger" 
                    dismissible 
                    onClose={() => setError(null)} 
                    className="shadow-sm rounded-pill px-4 py-3 mb-4"
                >
                    {error}
                </Alert>
            )}
            {success && (
                <Alert 
                    variant="success" 
                    dismissible 
                    onClose={() => setSuccess(null)} 
                    className="shadow-sm rounded-pill px-4 py-3 mb-4"
                >
                    {success}
                </Alert>
            )}

            <Card 
                className="shadow-lg border-0" 
                style={{ 
                    maxWidth: '600px', 
                    margin: '0 auto', 
                    borderRadius: '20px', 
                    background: 'linear-gradient(to bottom, #ffffff, #f8f9fa)',
                    transition: 'box-shadow 0.3s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.15)'}
                onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)'}
            >
                <Card.Body className="p-4">
                    {user.role === 'DOCTOR' && !payment && (
                        <Form onSubmit={handleCreatePayment}>
                            <Form.Group className="mb-4">
                                <Form.Label className="fw-semibold">Số Tiền (VND)</Form.Label>
                                <Form.Control
                                    type="number"
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    placeholder="Nhập số tiền"
                                    required
                                    className="border-primary rounded-pill"
                                    style={{ 
                                        padding: '0.75rem', 
                                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                                    }}
                                />
                            </Form.Group>
                            <div className="text-center">
                                <Button 
                                    variant="primary" 
                                    type="submit" 
                                    disabled={loading}
                                    className="rounded-pill px-5 py-2 shadow-sm"
                                    style={{ 
                                        backgroundColor: '#0d6efd', 
                                        borderColor: '#0d6efd',
                                        transition: 'transform 0.2s'
                                    }}
                                    onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                                    onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                                >
                                    {loading ? (
                                        <>
                                            <Spinner animation="border" size="sm" className="me-2" />
                                            Đang Xử Lý...
                                        </>
                                    ) : (
                                        'Tạo Hóa Đơn'
                                    )}
                                </Button>
                            </div>
                        </Form>
                    )}

                    {user.role === 'DOCTOR' && payment && (
                        <div>
                            <h5 className="fw-bold text-primary mb-3" style={{ fontSize: '1.4rem' }}>
                                Thông Tin Hóa Đơn
                            </h5>
                            <p className="mb-2" style={{ fontSize: '1.05rem' }}>
                                <strong>Số Tiền:</strong> {payment.amount} VND
                            </p>
                            <p className="mb-3" style={{ fontSize: '1.05rem' }}>
                                <strong>Trạng Thái:</strong>{' '}
                                <span
                                    style={{
                                        display: 'inline-block',
                                        padding: '4px 12px',
                                        borderRadius: '20px',
                                        fontSize: '0.85rem',
                                        fontWeight: '600',
                                        color: '#fff',
                                        backgroundColor:
                                            payment.paymentStatus === 'PENDING' ? '#ffc107' :
                                            payment.paymentStatus === 'COMPLETED' ? '#20c997' :
                                            '#dc3545'
                                    }}
                                >
                                    {payment.paymentStatus === 'PENDING' ? 'Đang Chờ' :
                                     payment.paymentStatus === 'COMPLETED' ? 'Hoàn Thành' :
                                     'Thất Bại'}
                                </span>
                            </p>
                        </div>
                    )}

                    {user.role === 'PATIENT' && !payment && (
                        <Alert 
                            variant="info" 
                            className="shadow-sm rounded-pill px-4 py-3"
                        >
                            Hóa đơn chưa được tạo. Vui lòng chờ bác sĩ tạo hóa đơn.
                        </Alert>
                    )}

                    {user.role === 'PATIENT' && payment && (
                        <div>
                            <h5 className="fw-bold text-primary mb-3" style={{ fontSize: '1.4rem' }}>
                                Thông Tin Thanh Toán
                            </h5>
                            <p className="mb-2" style={{ fontSize: '1.05rem' }}>
                                <strong>Số Tiền:</strong> {payment.amount} VND
                            </p>
                            <p className="mb-3" style={{ fontSize: '1.05rem' }}>
                                <strong>Trạng Thái:</strong>{' '}
                                <span
                                    style={{
                                        display: 'inline-block',
                                        padding: '4px 12px',
                                        borderRadius: '20px',
                                        fontSize: '0.85rem',
                                        fontWeight: '600',
                                        color: '#fff',
                                        backgroundColor:
                                            payment.paymentStatus === 'PENDING' ? '#ffc107' :
                                            payment.paymentStatus === 'COMPLETED' ? '#20c997' :
                                            '#dc3545'
                                    }}
                                >
                                    {payment.paymentStatus === 'PENDING' ? 'Đang Chờ' :
                                     payment.paymentStatus === 'COMPLETED' ? 'Hoàn Thành' :
                                     'Thất Bại'}
                                </span>
                            </p>
                            {payment.paymentStatus === 'PENDING' && (
                                <>
                                    <Form.Group className="mb-4">
                                        <Form.Label className="fw-semibold">Phương Thức Thanh Toán</Form.Label>
                                        <Form.Select
                                            value={paymentMethod}
                                            onChange={(e) => setPaymentMethod(e.target.value)}
                                            className="border-primary rounded-pill"
                                            style={{ 
                                                padding: '0.75rem', 
                                                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                                            }}
                                        >
                                            <option value="Momo">MoMo</option>
                                            <option value="VNPay">VNPay</option>
                                        </Form.Select>
                                    </Form.Group>
                                    <div className="text-center">
                                        <Button 
                                            variant="success" 
                                            onClick={handleProcessPayment} 
                                            disabled={loading}
                                            className="rounded-pill px-5 py-2 shadow-sm"
                                            style={{ 
                                                backgroundColor: '#20c997', 
                                                borderColor: '#20c997',
                                                transition: 'transform 0.2s'
                                            }}
                                            onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                                            onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                                        >
                                            {loading ? (
                                                <>
                                                    <Spinner animation="border" size="sm" className="me-2" />
                                                    Đang Xử Lý...
                                                </>
                                            ) : (
                                                'Thanh Toán'
                                            )}
                                        </Button>
                                    </div>
                                </>
                            )}
                        </div>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Payment;