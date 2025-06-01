import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import cookie from 'react-cookies';
import { useMyUser } from '../configs/MyContexts';

const Appointment = () => {
  const { user } = useMyUser() || {};
  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(true);
  const [loadingCancel, setLoadingCancel] = useState(false);
  const [loadingReschedule, setLoadingReschedule] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState({
    status: '',
    startDate: '',
    endDate: '',
  });
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [newDateTime, setNewDateTime] = useState('');

  // Để lưu trạng thái đánh giá cho từng lịch hẹn
  const [appointmentRatings, setAppointmentRatings] = useState({});
  const [ratingLoading, setRatingLoading] = useState(false);

  const loadAppointments = async () => {
    if (!user) return;

    try {
      setLoadingAppointments(true);
      let url = `${endpoints['appointmentsFilter']}?page=${page}`;

      if (user.role === 'PATIENT') {
        url += `&patientId=${user.id}`;
      } else if (user.role === 'DOCTOR') {
        url += `&doctorId=${user.id}`;
      }
      if (filters.status) url += `&status=${filters.status}`;
      if (filters.startDate) url += `&startDate=${new Date(filters.startDate).toISOString()}`;
      if (filters.endDate) url += `&endDate=${new Date(filters.endDate).toISOString()}`;

      const res = await authApis().get(url);
      const appointmentsData = res.data;

      const appointmentsWithPayment = await Promise.all(
        appointmentsData.map(async (appt) => {
          if (appt.status === 'COMPLETED') {
            try {
              const paymentRes = await Apis.get(`${endpoints['payment']}/appointment/${appt.id}`);
              return { ...appt, payment: paymentRes.data };
            } catch (ex) {
              if (ex.response?.status === 404) {
                return { ...appt, payment: null };
              }
              throw ex;
            }
          }
          return { ...appt, payment: null };
        })
      );

      if (appointmentsWithPayment.length === 0) setPage(0);
      else {
        if (page === 1) setAppointments(appointmentsWithPayment);
        else setAppointments([...appointments, ...appointmentsWithPayment]);
      }
    } catch (ex) {
      console.error('Load appointments error:', ex);
      setError(`Không thể tải danh sách lịch hẹn: ${ex.message || ex}`);
    } finally {
      setLoadingAppointments(false);
    }
  };

  useEffect(() => {
    if (page > 0 && user) loadAppointments();


  }, [page, filters, user]);

  const loadMore = () => {
    if (!loadingAppointments && page > 0) {
      setPage(page + 1);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
    setPage(1);
    setAppointments([]);
  };

  const clearFilters = () => {
    setFilters({ status: '', startDate: '', endDate: '' });
    setPage(1);
    setAppointments([]);
  };

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const cancelAppointment = async (appointmentId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy lịch hẹn này?')) return;

    try {
      setLoadingCancel(true);
      const token = cookie.load('token');
      const url = endpoints['cancelAppointment'](appointmentId);
      await authApis().patch(url);
      await loadAppointments();
      setSuccess('Hủy lịch hẹn thành công!');
      setTimeout(() => setSuccess(null), 2000);
    } catch (ex) {
      console.error('Cancel error:', ex);
      let errorMessage = 'Hủy lịch hẹn thất bại: ';
      if (ex.response) {
        errorMessage += ex.response.data || ex.response.statusText;
      } else if (ex.request) {
        errorMessage += 'Không nhận được phản hồi từ server';
      } else {
        errorMessage += ex.message;
      }
      setError(errorMessage);
    } finally {
      setLoadingCancel(false);
    }
  };

  const openRescheduleModal = (appointment) => {
    setSelectedAppointment(appointment);
    setNewDateTime('');
    setShowRescheduleModal(true);
  };

  const closeRescheduleModal = () => {
    setShowRescheduleModal(false);
    setSelectedAppointment(null);
  };

  const rescheduleAppointment = async () => {
    if (!newDateTime) {
      setError('Vui lòng chọn ngày giờ mới.');
      return;
    }

    try {
      setLoadingReschedule(true);
      const token = cookie.load('token');
      const body = { newDateTime: new Date(newDateTime).toISOString() };
      const url = endpoints['rescheduleAppointment'](selectedAppointment.id);
      await authApis().patch(url, body);
      await loadAppointments();
      setSuccess('Đổi lịch hẹn thành công!');
      setTimeout(() => setSuccess(null), 2000);
      closeRescheduleModal();
    } catch (ex) {
      console.error('Reschedule error:', ex);
      let errorMessage = 'Đổi lịch hẹn thất bại: ';
      if (ex.response) {
        errorMessage += ex.response.data || ex.response.statusText;
      } else if (ex.request) {
        errorMessage += 'Không nhận được phản hồi từ server';
      } else {
        errorMessage += ex.message;
      }
      setError(errorMessage);
    } finally {
      setLoadingReschedule(false);
    }
  };


  return (
    <Container className="py-5">
      <h2 
        className="fw-bold mb-4" 
        style={{ 
          color: '#0d6efd', 
          fontSize: '2.2rem', 
          textShadow: '1px 1px 2px rgba(0,0,0,0.1)' 
        }}
      >
        Lịch Hẹn Của Bạn
      </h2>

      {!user && (
        <Alert 
          variant="warning" 
          className="shadow-sm rounded-pill px-4 py-3"
        >
          Vui lòng đăng nhập để xem danh sách lịch hẹn!{' '}
          <Link to="/login" className="ms-2 text-decoration-none fw-semibold">
            Đăng Nhập
          </Link>
        </Alert>
      )}

      {user && (
        <>
          {/* Filter Form */}
          <Card 
            className="mb-5 shadow-lg border-0" 
            style={{ 
              borderRadius: '20px', 
              background: 'linear-gradient(to right, #f8f9fa, #e9ecef)',
              transition: 'box-shadow 0.3s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.15)'}
            onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)'}
          >
            <Card.Body className="p-4">
              <Form>
                <Row className="g-4">
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label className="fw-semibold">Trạng Thái</Form.Label>
                      <Form.Select
                        name="status"
                        value={filters.status}
                        onChange={handleFilterChange}
                        className="border-primary rounded-pill"
                        style={{ padding: '0.75rem' }}
                      >
                        <option value="">Tất Cả</option>
                        <option value="PENDING">Đang Chờ</option>
                        <option value="CONFIRMED">Đã Xác Nhận</option>
                        <option value="COMPLETED">Hoàn Thành</option>
                        <option value="CANCELED">Đã Hủy</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label className="fw-semibold">Từ Ngày</Form.Label>
                      <Form.Control
                        type="date"
                        name="startDate"
                        value={filters.startDate}
                        onChange={handleFilterChange}
                        className="border-primary rounded-pill"
                        style={{ padding: '0.75rem' }}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label className="fw-semibold">Đến Ngày</Form.Label>
                      <Form.Control
                        type="date"
                        name="endDate"
                        value={filters.endDate}
                        onChange={handleFilterChange}
                        min={filters.startDate}
                        className="border-primary rounded-pill"
                        style={{ padding: '0.75rem' }}
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <div className="mt-4 text-end">
                  <Button
                    variant="outline-secondary"
                    onClick={clearFilters}
                    className="rounded-pill px-4 py-2"
                    style={{ 
                      transition: 'transform 0.2s',
                      borderColor: '#6c757d'
                    }}
                    onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                    onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                  >
                    Xóa Bộ Lọc
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>

          {error && (
            <Alert 
              variant="danger" 
              onClose={() => setError(null)} 
              dismissible 
              className="shadow-sm rounded-pill px-4 py-3 mb-4"
            >
              {error}
            </Alert>
          )}
          {success && (
            <Alert 
              variant="success" 
              onClose={() => setSuccess(null)} 
              dismissible 
              className="shadow-sm rounded-pill px-4 py-3 mb-4"
            >
              {success}
            </Alert>
          )}

          {loadingAppointments && page === 1 && (
            <div className="text-center my-5">
              <Spinner 
                animation="border" 
                variant="primary" 
                style={{ width: '3rem', height: '3rem' }}
              />
              <p className="mt-3 fw-semibold" style={{ color: '#0d6efd' }}>
                Đang tải lịch hẹn...
              </p>
            </div>
          )}

          {appointments.length === 0 && !loadingAppointments && (
            <Alert 
              variant="info" 
              className="shadow-sm rounded-pill px-4 py-3"
            >
              Bạn chưa có lịch hẹn nào!
            </Alert>
          )}

          {appointments.length > 0 && (
            <Row xs={1} md={2} lg={3} className="g-4">
              {appointments.map((appt) => (
                <Col key={appt.id}>
                  <Card 
                    className="shadow-lg border-0 h-100" 
                    style={{ 
                      borderRadius: '20px', 
                      transition: 'transform 0.3s, box-shadow 0.3s'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.transform = 'translateY(-5px)';
                      e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.15)';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = 'translateY(0)';
                      e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
                    }}
                  >
                    <Card.Body className="d-flex flex-column p-4">
                      <div className="d-flex align-items-center mb-3">
                        <img
                          src={
                            user.role === 'PATIENT'
                              ? appt.doctor.user.avatar || '/images/doctor-placeholder.jpg'
                              : appt.patient.user.avatar || '/images/patient-placeholder.jpg'
                          }
                          alt="Avatar"
                          style={{
                            width: '60px',
                            height: '60px',
                            borderRadius: '50%',
                            objectFit: 'cover',
                            marginRight: '15px',
                            border: '2px solid #0d6efd',
                            transition: 'transform 0.3s'
                          }}
                          onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                          onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                        />
                        <div>
                          <h5 className="fw-bold text-primary mb-1" style={{ fontSize: '1.2rem' }}>
                            {user.role === 'PATIENT'
                              ? `${appt.doctor.user.firstName} ${appt.doctor.user.lastName}`
                              : `${appt.patient.user.firstName} ${appt.patient.user.lastName}`}
                          </h5>
                          <p className="text-muted mb-0" style={{ fontSize: '0.95rem' }}>
                            {user.role === 'PATIENT' ? 'Bác Sĩ' : 'Bệnh Nhân'}
                          </p>
                        </div>
                      </div>
                      <p className="mb-2" style={{ fontSize: '0.95rem' }}>
                        <strong>Ngày Hẹn:</strong> {formatDate(appt.appointmentDate)}
                      </p>
                      <p className="mb-3" style={{ fontSize: '0.95rem' }}>
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
                              appt.status === 'PENDING' ? '#ffc107' :
                              appt.status === 'CONFIRMED' ? '#0d6efd' :
                              appt.status === 'COMPLETED' ? '#20c997' :
                              '#dc3545'
                          }}
                        >
                          {appt.status === 'PENDING' ? 'Đang Chờ' :
                           appt.status === 'CONFIRMED' ? 'Đã Xác Nhận' :
                           appt.status === 'COMPLETED' ? 'Hoàn Thành' :
                           'Đã Hủy'}
                        </span>
                      </p>
                      <div className="mt-auto d-flex flex-wrap gap-2">
                        {user.role === 'PATIENT' && (appt.status === 'PENDING' ||  appt.status === 'CONFIRMED') &&(
                          <>
                            <Button
                              variant="warning"
                              size="sm"
                              className="rounded-pill px-3"
                              onClick={() => openRescheduleModal(appt)}
                              style={{ transition: 'transform 0.2s' }}
                              onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                              onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                            >
                              Đổi Lịch
                            </Button>
                            <Button
                              variant="danger"
                              size="sm"
                              className="rounded-pill px-3"
                              onClick={() => cancelAppointment(appt.id)}
                              disabled={loadingCancel}
                              style={{ transition: 'transform 0.2s' }}
                              onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                              onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                            >
                              {loadingCancel ? (
                                <>
                                  <Spinner animation="border" size="sm" className="me-2" />
                                  Đang Xử Lý...
                                </>
                              ) : (
                                'Hủy'
                              )}
                            </Button>
                          </>
                        )}
                        {user.role === 'DOCTOR' && appt.status === 'COMPLETED' && !appt.payment && (
                          <Button
                            as={Link}
                            to={`/payment/${appt.id}`}
                            variant="primary"
                            size="sm"
                            className="rounded-pill px-3"
                            style={{ 
                              backgroundColor: '#0d6efd', 
                              borderColor: '#0d6efd',
                              transition: 'transform 0.2s'
                            }}
                            onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                            onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                          >
                            Tạo Hóa Đơn
                          </Button>
                        )}

                        {user.role === 'DOCTOR' && appt.status === 'CONFIRMED' &&(
                            <Button
                            as={Link}
                            to={`/health-record/create/${appt.id}`}
                            variant="info"
                            size="sm"
                            className="rounded-pill px-3"
                            disabled={!user.isVerified}
                            style={{ backgroundColor: "#0dcaf0", borderColor: "#0dcaf0" }}
                            >
                            Tạo Kết Quả Khám
                            </Button>
                        )}
                        {user.role === 'PATIENT' && appt.status === 'COMPLETED' && appt.payment && (
                          <Button
                            as={Link}
                            to={`/payment/${appt.id}`}
                            variant="success"
                            size="sm"
                            className="rounded-pill px-3"
                            disabled={appt.payment.paymentStatus !== 'PENDING'}
                            style={{ 
                              backgroundColor: '#20c997', 
                              borderColor: '#20c997',
                              transition: 'transform 0.2s'
                            }}
                            onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                            onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                          >
                            Thanh Toán
                          </Button>
                        )}
                        {appt.status === 'CONFIRMED' && (
                          <Button
                            as={Link}
                            to={`/chat/${user.role === 'PATIENT' ? appt.doctor.id : appt.patient.id}`}
                            variant="primary"
                            size="sm"
                            className="rounded-pill px-3"
                            aria-label={`Chat với ${user.role === 'PATIENT' ? 'bác sĩ' : 'bệnh nhân'}`}
                            style={{ 
                              backgroundColor: '#0d6efd', 
                              borderColor: '#0d6efd',
                              transition: 'transform 0.2s'
                            }}
                            onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                            onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                          >
                            Chat
                          </Button>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}

          {page > 0 && !loadingAppointments && (
            <div className="text-center mt-5">
              <Button
                variant="primary"
                onClick={loadMore}
                className="px-5 py-2 rounded-pill shadow-sm"
                style={{ 
                  backgroundColor: '#0d6efd', 
                  borderColor: '#0d6efd',
                  transition: 'transform 0.2s'
                }}
                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
              >
                Xem Thêm
              </Button>
            </div>
          )}

          {/* Reschedule Modal */}
          <Modal 
            show={showRescheduleModal} 
            onHide={closeRescheduleModal}
            centered
          >
            <Modal.Header 
              closeButton 
              className="bg-primary text-white"
              style={{ borderTopLeftRadius: '10px', borderTopRightRadius: '10px' }}
            >
              <Modal.Title className="fw-bold">Đổi Lịch Hẹn</Modal.Title>
            </Modal.Header>
            <Modal.Body className="p-4">
              <Form>
                <Form.Group className="mb-3">
                  <Form.Label className="fw-semibold">Chọn Ngày Giờ Mới</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={newDateTime}
                    onChange={(e) => setNewDateTime(e.target.value)}
                    min={new Date().toISOString().slice(0, 16)}
                    className="border-primary rounded-pill"
                    style={{ padding: '0.75rem' }}
                  />
                </Form.Group>
              </Form>
            </Modal.Body>
            <Modal.Footer className="border-0">
              <Button 
                variant="secondary" 
                onClick={closeRescheduleModal} 
                className="rounded-pill px-4 py-2"
                style={{ transition: 'transform 0.2s' }}
                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
              >
                Đóng
              </Button>
              <Button
                variant="primary"
                onClick={rescheduleAppointment}
                disabled={loadingReschedule || !newDateTime}
                className="rounded-pill px-4 py-2"
                style={{ 
                  backgroundColor: '#0d6efd', 
                  borderColor: '#0d6efd',
                  transition: 'transform 0.2s'
                }}
                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
              >
                {loadingReschedule ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Đang Xử Lý...
                  </>
                ) : (
                  'Lưu Thay Đổi'
                )}
              </Button>
            </Modal.Footer>
          </Modal>
        </>
      )}
    </Container>
  );
};

export default Appointment;