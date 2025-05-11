import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Nav, Navbar, NavDropdown, Row, Spinner } from 'react-bootstrap';
import Apis, { endpoints } from '../configs/Apis';
import { useSearchParams } from 'react-router-dom';

const Home = () => {
    const [doctors, setDoctors] = useState([]);

    const [loading, setLoading] = useState(true);

    const [page, setPage] = useState(1);

    const [q] = useSearchParams();

    const loadDoctors = async () => {
        try{
            setLoading(true);

            let url = `${endpoints['doctors']}?page=${page}`

            let hospId = q.get('hospital');
            let specId = q.get('specialization');
            let doctorName = q.get('doctorName');
            if(hospId) {
                url = `${url}&hospital=${hospId}`;
            }

            if(specId) {
                url = `${url}&specialization=${specId}`;
            }
            
            if(doctorName){
                url = `${url}&doctorName=${doctorName}`;
            }

            let res = await Apis.get(url);
            if (res.data.length === 0)
                setPage(0);
            else{
                if(page === 1)
                    setDoctors(res.data)
                else
                    setDoctors([...doctors, ...res.data])
            }
        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (page > 0)
            loadDoctors();
    }, [page, q]);

    useEffect(() => {
        setPage(1);
        setDoctors([]);
    }, [q]);

    const loadMore = () => {
        if (!loading && page > 0)
            setPage(page + 1);
        
    }

    return ( 
        <>
            {loading && <Spinner animation="border" variant="primary" />}

            {doctors.length === 0 && <Alert variant="info" className="mt-2">Không có bác sĩ nào!</Alert>}

            <Row>
                {doctors.map(d => <Col className="p-2" md={3} xs={6} key={d.id}>
                    <Card>
                        <Card.Img variant="top" src={d.user.avatar || ''} style={{ width: '100%', height: '200px', objectFit: 'cover' }}  />
                        <Card.Body>
                            <Card.Title>{d.user.firstName} {d.user.lastName}</Card.Title>
                            <Card.Text>{d.phoneNumber}</Card.Text>
                            <Card.Text>{d.specialization.name}</Card.Text>
                            <Card.Text>{d.hospital.name}</Card.Text>
                            <Button className="me-1" variant="primary">Xem chi tiết</Button>
                            <Button variant="danger">Đặt lịch</Button>
                        </Card.Body>
                    </Card>
                </Col>)}
            </Row>
            {page > 0 && <div className="text-center mb-2">
                <Button variant="info" onClick={loadMore}>Xem thêm</Button>
            </div>}
            
       </>
    );
}

export default Home;