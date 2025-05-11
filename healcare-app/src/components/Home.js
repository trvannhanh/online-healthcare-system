import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Row, Spinner } from 'react-bootstrap';
import Apis, { endpoints } from '../configs/Apis';
import { Link, useSearchParams } from 'react-router-dom';

const Home = () => {
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [q] = useSearchParams();

    const loadDoctors = async () => {
        try {
            setLoading(true);
            let url = `${endpoints['doctors']}?page=${page}`;

            let hospId = q.get('hospital');
            let specId = q.get('specialization');
            let doctorName = q.get('doctorName');

            if (hospId) url += `&hospital=${hospId}`;
            if (specId) url += `&specialization=${specId}`;
            if (doctorName) url += `&doctorName=${doctorName}`;

            let res = await Apis.get(url);
            if (res.data.length === 0)
                setPage(0);
            else {
                if (page === 1)
                    setDoctors(res.data);
                else
                    setDoctors([...doctors, ...res.data]);
            }
        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (page > 0) loadDoctors();
    }, [page, q]);

    useEffect(() => {
        setPage(1);
        setDoctors([]);
    }, [q]);

    const loadMore = () => {
        if (!loading && page > 0)
            setPage(page + 1);
    };

    return (
        <>
            {/* Hero Section */}
            <div
                className="text-white py-5 px-4 mb-4"
                style={{
                    background: "linear-gradient(rgba(13,110,253,0.8), rgba(13,110,253,0.8)), url('/images/hero-doctor.jpg') center/cover no-repeat",
                    borderRadius: '12px'
                }}
            >
                <Container>
                    <h1 className="display-5 fw-bold">Tìm bác sĩ phù hợp với bạn</h1>
                    <p className="lead">Chọn theo chuyên khoa, bệnh viện hoặc tên bác sĩ để được hỗ trợ tốt nhất.</p>
                </Container>
            </div>

            <Container>
                {loading && <div className="text-center my-4"><Spinner animation="border" variant="primary" /></div>}
                {doctors.length === 0 && !loading && <Alert variant="info" className="mt-2">Không có bác sĩ nào!</Alert>}

                <Row>
                    {doctors.map(d => (
                        <Col key={d.id} className="mb-4" md={4} lg={3} sm={6}>
                            <Card className="h-100 shadow-sm border-0" style={{ borderRadius: '16px' }}>
                                <Card.Img
                                    variant="top"
                                    src={d.user.avatar || '/images/doctor-placeholder.jpg'}
                                    style={{
                                        width: '100%',
                                        height: '220px',
                                        objectFit: 'cover',
                                        borderTopLeftRadius: '16px',
                                        borderTopRightRadius: '16px'
                                    }}
                                />
                                <Card.Body className="d-flex flex-column justify-content-between">
                                    <div>
                                        <Card.Title className="fs-5 text-primary">{d.user.firstName} {d.user.lastName}</Card.Title>
                                        <Card.Text className="mb-1"><strong>📞</strong> {d.phoneNumber}</Card.Text>
                                        <Card.Text className="mb-1"><strong>🏥</strong> {d.hospital.name}</Card.Text>
                                        <Card.Text className="mb-2"><strong>🩺</strong> {d.specialization.name}</Card.Text>
                                    </div>
                                    <div className="mt-auto d-flex justify-content-between">
                                        <Button as={Link} to={`/doctors/${d.id}`} variant="outline-primary" size="sm">Xem chi tiết</Button>
                                        <Button as={Link} to={`/appointments/new?doctorId=${d.id}`} variant="success" size="sm">Đặt lịch</Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>

                {page > 0 && !loading &&
                    <div className="text-center mb-4">
                        <Button variant="primary" onClick={loadMore} className="px-4 py-2 rounded-pill shadow-sm">
                            Xem thêm
                        </Button>
                    </div>
                }
            </Container>
        </>
    );
};

export default Home;