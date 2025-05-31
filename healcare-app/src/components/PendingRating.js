import { useState, useEffect } from 'react';
import { Container, Alert, Card, Row, Col, Button, Spinner, Badge, ProgressBar } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Apis, { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';

const PendingRating = () => {
  const { user } = useMyUser();
  const [pendingRatings, setPendingRatings] = useState([]);
  const [completedRatings, setCompletedRatings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [ratingLoading, setRatingLoading] = useState(false);

  const [timeRemaining, setTimeRemaining] = useState({});


  useEffect(() => {
    if (user?.role === 'PATIENT') {
      loadPendingRatings();
    }
  }, [user]);

  useEffect(() => {
    if (completedRatings.length === 0) return;

    const timer = setInterval(() => {
      const now = new Date();
      const updatedTimeRemaining = {};

      completedRatings.forEach(appointment => {
        if (appointment.ratingInfo && appointment.canEdit) {
          const ratingDate = new Date(appointment.ratingInfo.ratingDate || appointment.ratingInfo.createdAt);
          const diffInMinutes = 30 - ((now - ratingDate) / (1000 * 60));

          if (diffInMinutes > 0) {
            updatedTimeRemaining[appointment.id] = {
              minutes: Math.floor(diffInMinutes),
              seconds: Math.floor((diffInMinutes % 1) * 60),
              percentage: (diffInMinutes / 30) * 100
            };
          } else {
            // Thời gian đã hết
            updatedTimeRemaining[appointment.id] = {
              minutes: 0,
              seconds: 0,
              percentage: 0
            };

            // Cập nhật lại trạng thái canEdit
            setCompletedRatings(prev => prev.map(item =>
              item.id === appointment.id ? { ...item, canEdit: false } : item
            ));
          }
        }
      });

      setTimeRemaining(updatedTimeRemaining);
    }, 1000);

    return () => clearInterval(timer);
  }, [completedRatings]);

  const loadPendingRatings = async () => {
    try {
      setLoading(true);
      // Lấy tất cả lịch hẹn đã hoàn thành của bệnh nhân
      const response = await authApis().get(endpoints['appointmentsFilter'], {
        params: {
          patientId: user.id,
          status: 'COMPLETED',
          page: 1
        }
      });

      const appointments = response.data;

      // Phân loại các lịch hẹn đã/chưa đánh giá
      const pendingArray = [];
      const completedArray = [];

      await Promise.all(appointments.map(async (appt) => {
        try {
          const isRatedRes = await Apis.get(endpoints['isAppointmentRated'](appt.id));
          if (isRatedRes.data) {
            // Nếu đã đánh giá, lấy thêm thông tin chi tiết
            try {
              const ratingDetails = await Apis.get(endpoints['appointmentRating'](appt.id));
              const ratingDate = new Date(ratingDetails.data.ratingDate || ratingDetails.data.createdAt);
              const now = new Date();
              const diffInMinutes = (now - ratingDate) / (1000 * 60);

              completedArray.push({
                ...appt,
                ratingInfo: ratingDetails.data,
                canEdit: diffInMinutes <= 30 // Cho phép sửa trong vòng 30 phút
              });
            } catch (err) {
              completedArray.push(appt);
            }
          } else {
            pendingArray.push(appt);
          }
        } catch (err) {
          console.error(`Error checking rating for appointment ${appt.id}:`, err);
        }
      }));

      setPendingRatings(pendingArray);
      setCompletedRatings(completedArray);
    } catch (err) {
      console.error('Error loading pending ratings:', err);
      setError('Không thể tải danh sách đánh giá. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
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

  if (!user || user.role !== 'PATIENT') {
    return (
      <Container className="py-5">
        <Alert variant="warning">
          Trang này chỉ dành cho bệnh nhân. Vui lòng đăng nhập với tài khoản bệnh nhân.
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <h2 className="mb-4 text-center">Đánh giá bác sĩ</h2>

      {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}
      {success && <Alert variant="success" onClose={() => setSuccess(null)} dismissible>{success}</Alert>}

      {loading ? (
        <div className="text-center my-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-2">Đang tải dữ liệu...</p>
        </div>
      ) : (
        <>
          <h3 className="mb-3">Lịch hẹn chờ đánh giá</h3>
          {pendingRatings.length === 0 ? (
            <Alert variant="info">Không có lịch hẹn nào đang chờ đánh giá.</Alert>
          ) : (
            <Row xs={1} md={2} lg={3} className="g-4 mb-5">
              {pendingRatings.map((appointment) => (
                <Col key={appointment.id}>
                  <Card className="h-100 shadow-sm">
                    <Card.Body>
                      <div className="d-flex align-items-center mb-3">
                        <img
                          src={appointment.doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                          alt="Bác sĩ"
                          className="rounded-circle me-3"
                          style={{ width: '60px', height: '60px', objectFit: 'cover' }}
                        />
                        <div>
                          <h5 className="mb-1">BS. {appointment.doctor.user.firstName} {appointment.doctor.user.lastName}</h5>
                          <p className="text-muted mb-0">{appointment.doctor.specialization.name}</p>
                        </div>
                      </div>
                      <p><strong>Ngày khám:</strong> {formatDate(appointment.appointmentDate)}</p>
                      <div className="mt-3 text-center">
                        <Button
                          as={Link}
                          to={`/rate-doctor/${appointment.id}`}
                          variant="primary"
                          className="w-100"
                        >
                          Đánh giá ngay
                        </Button>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}

          <h3 className="mb-3">Đánh giá gần đây</h3>
          {completedRatings.length === 0 ? (
            <Alert variant="info">Bạn chưa có đánh giá nào.</Alert>
          ) : (
            <Row xs={1} md={2} lg={3} className="g-4">
              {completedRatings.map((appointment) => (
                <Col key={appointment.id}>
                  <Card className="h-100 shadow-sm">
                    <Card.Body>
                      <div className="d-flex align-items-center mb-3">
                        <img
                          src={appointment.doctor.user.avatar || '/images/doctor-placeholder.jpg'}
                          alt="Bác sĩ"
                          className="rounded-circle me-3"
                          style={{ width: '60px', height: '60px', objectFit: 'cover' }}
                        />
                        <div>
                          <h5 className="mb-1">BS. {appointment.doctor.user.firstName} {appointment.doctor.user.lastName}</h5>
                          <p className="text-muted mb-0">{appointment.doctor.specialization.name}</p>
                        </div>
                      </div>
                      <p><strong>Ngày khám:</strong> {formatDate(appointment.appointmentDate)}</p>
                      <p><strong>Đánh giá:</strong> {appointment.ratingInfo?.rating}/5</p>
                      <p><strong>Nhận xét:</strong> {appointment.ratingInfo?.comment || 'Không có nhận xét'}</p>

                      {appointment.canEdit && (
                        <>
                          {/* Thêm hiển thị thời gian còn lại */}
                          <div className="my-3">
                            <small className="text-muted d-block mb-1">Thời gian chỉnh sửa còn lại:</small>
                            <div className="d-flex align-items-center">
                              <Badge bg="info" className="me-2">
                                {timeRemaining[appointment.id]?.minutes || 0}:{String(timeRemaining[appointment.id]?.seconds || 0).padStart(2, '0')}
                              </Badge>
                              <ProgressBar
                                now={timeRemaining[appointment.id]?.percentage || 0}
                                variant={
                                  (timeRemaining[appointment.id]?.minutes || 0) > 10 ? "success" :
                                    (timeRemaining[appointment.id]?.minutes || 0) > 5 ? "warning" : "danger"
                                }
                                style={{ height: '8px', flexGrow: 1 }}
                              />
                            </div>
                          </div>

                          {/* Nút chỉnh sửa và xóa */}
                          <div className="mt-3 d-flex justify-content-between">
                            <Button
                              as={Link}
                              to={`/rate-doctor/${appointment.id}`}
                              variant="outline-primary"
                              size="sm"
                            >
                              Chỉnh sửa
                            </Button>
                          </div>
                        </>
                      )}
                    </Card.Body>
                    <Card.Footer className="bg-white text-muted">
                      <small>Đánh giá vào: {formatDate(appointment.ratingInfo?.ratingDate || appointment.ratingInfo?.createdAt)}</small>
                    </Card.Footer>
                  </Card>
                </Col>
              ))}
            </Row>

          )}
        </>
      )}
    </Container>
  );
};
export default PendingRating;