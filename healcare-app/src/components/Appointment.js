import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Form, Modal, Row, Spinner } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';
import cookie from 'react-cookies';

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

      // Fetch payment info for COMPLETED appointments
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
      <link
        href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css"
        rel="stylesheet"
      />
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Lịch Hẹn Của Bạn</h2>

      {!user && (
        <Alert variant="warning">
          Vui lòng đăng nhập để xem danh sách lịch hẹn!
          <Link to="/login" className="ms-2">Đăng nhập</Link>
        </Alert>
      )}

      {user && (
        <>
          {/* Filter Form */}
          <Card className="mb-4 shadow-sm border-0 rounded-lg">
            <Card.Body>
              <Form>
                <Row className="g-3">
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label>Trạng thái</Form.Label>
                      <Form.Select
                        name="status"
                        value={filters.status}
                        onChange={handleFilterChange}
                      >
                        <option value="">Tất cả</option>
                        <option value="PENDING">Đang chờ</option>
                        <option value="CONFIRMED">Đã xác nhận</option>
                        <option value="COMPLETED">Hoàn thành</option>
                        <option value="CANCELED">Đã hủy</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label>Từ ngày</Form.Label>
                      <Form.Control
                        type="date"
                        name="startDate"
                        value={filters.startDate}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group>
                      <Form.Label>Đến ngày</Form.Label>
                      <Form.Control
                        type="date"
                        name="endDate"
                        value={filters.endDate}
                        onChange={handleFilterChange}
                        min={filters.startDate}
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <div className="mt-3">
                  <Button
                    variant="outline-secondary"
                    onClick={clearFilters}
                    className="rounded-full"
                  >
                    Xóa bộ lọc
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>

          {error && (
            <Alert variant="danger" onClose={() => setError(null)} dismissible>
              {error}
            </Alert>
          )}
          {success && (
            <Alert variant="success" onClose={() => setSuccess(null)} dismissible>
              {success}
            </Alert>
          )}

          {loadingAppointments && page === 1 && (
            <div className="text-center my-4">
              <Spinner animation="border" variant="primary" />
            </div>
          )}

          {appointments.length === 0 && !loadingAppointments && (
            <Alert variant="info">Bạn chưa có lịch hẹn nào!</Alert>
          )}

          {appointments.length > 0 && (
            <Row xs={1} md={2} lg={3} className="g-4">
              {appointments.map((appt) => (
                <Col key={appt.id}>
                  <Card className="shadow-sm border-0 rounded-lg h-full">
                    <Card.Body className="d-flex flex-column">
                      <div className="d-flex align-items-center mb-3">
                        <img
                          src={
                            user.role === 'PATIENT'
                              ? appt.doctor.user.avatar || '/images/doctor-placeholder.jpg'
                              : appt.patient.user.avatar || '/images/patient-placeholder.jpg'
                          }
                          alt="Avatar"
                          className="rounded-full w-12 h-12 object-cover mr-3"
                        />
                        <div>
                          <h5 className="font-semibold text-gray-800">
                            {user.role === 'PATIENT'
                              ? `${appt.doctor.user.firstName} ${appt.doctor.user.lastName}`
                              : `${appt.patient.user.firstName} ${appt.patient.user.lastName}`}
                          </h5>
                          <p className="text-sm text-gray-500">
                            {user.role === 'PATIENT' ? 'Bác sĩ' : 'Bệnh nhân'}
                          </p>
                        </div>
                      </div>
                      <p className="text-sm mb-1">
                        <strong>Ngày hẹn:</strong> {formatDate(appt.appointmentDate)}
                      </p>
                      <p className="text-sm mb-3">
                        <strong>Trạng thái:</strong>{' '}
                        <span
                          className={`inline-block px-2 py-1 rounded-full text-xs font-semibold
                            ${appt.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                              appt.status === 'CONFIRMED' ? 'bg-blue-100 text-blue-800' :
                                appt.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                                  'bg-red-100 text-red-800'}`}
                        >
                          {appt.status}
                        </span>
                      </p>
                      <div className="mt-auto flex flex-wrap gap-2">
                        {appt.status === 'PENDING' && (
                          <>
                            <Button
                              variant="warning"
                              size="sm"
                              className="rounded-full"
                              onClick={() => openRescheduleModal(appt)}
                            >
                              Đổi lịch
                            </Button>
                            <Button
                              variant="danger"
                              size="sm"
                              className="rounded-full"
                              onClick={() => cancelAppointment(appt.id)}
                              disabled={loadingCancel}
                            >
                              {loadingCancel ? (
                                <>
                                  <Spinner animation="border" size="sm" className="me-2" />
                                  Đang xử lý...
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
                            className="rounded-full"
                          >
                            Tạo hóa đơn
                          </Button>
                        )}
                        {user.role === 'PATIENT' && appt.status === 'COMPLETED' && appt.payment && (
                          <Button
                            as={Link}
                            to={`/payment/${appt.id}`}
                            variant="success"
                            size="sm"
                            className="rounded-full"
                            disabled={appt.payment.paymentStatus !== 'PENDING'}
                          >
                            Thanh toán
                          </Button>
                        )}
                        {appt.status === 'CONFIRMED' && (
                          <Link
                            to={`/chat/${user.role === 'PATIENT' ? appt.doctor.id : appt.patient.id}`}
                            aria-label={`Chat với ${user.role === 'PATIENT' ? 'bác sĩ' : 'bệnh nhân'}`}
                          >
                            <Button variant="primary" size="sm" className="rounded-full">
                              Chat
                            </Button>
                          </Link>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}

          {page > 0 && !loadingAppointments && (
            <div className="text-center mt-4">
              <Button
                variant="primary"
                onClick={loadMore}
                className="px-4 py-2 rounded-full shadow-sm"
              >
                Xem thêm
              </Button>
            </div>
          )}

          {/* Reschedule Modal */}
          <Modal show={showRescheduleModal} onHide={closeRescheduleModal}>
            <Modal.Header closeButton>
              <Modal.Title>Đổi lịch hẹn</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Form>
                <Form.Group className="mb-3">
                  <Form.Label>Chọn ngày giờ mới</Form.Label>
                  <Form.Control
                    type="datetime-local"
                    value={newDateTime}
                    onChange={(e) => setNewDateTime(e.target.value)}
                    min={new Date().toISOString().slice(0, 16)}
                  />
                </Form.Group>
              </Form>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={closeRescheduleModal} className="rounded-full">
                Đóng
              </Button>
              <Button
                variant="primary"
                onClick={rescheduleAppointment}
                disabled={loadingReschedule || !newDateTime}
                className="rounded-full"
              >
                {loadingReschedule ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Đang xử lý...
                  </>
                ) : (
                  'Lưu thay đổi'
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