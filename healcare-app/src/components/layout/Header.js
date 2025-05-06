import { useEffect, useState } from "react";
import { Button, Col, Container, Form, InputGroup, Nav, Navbar, NavDropdown, Row } from "react-bootstrap";
import Apis, { endpoint } from "../../configs/Apis";
import { Link, useNavigate } from "react-router-dom";


const Header = () =>{
    const [hospitals, setHospitals] = useState([]);

    const nav = useNavigate();
    const [doctorName, setDoctorName] = useState();

    const [specialization, setSpecialization] = useState([]);

    const loadHospitals = async () =>{
        let res = await Apis.get(endpoint['hospitals']);
        setHospitals(res.data);
    }

    const loadSpecialization = async () =>{
        let res = await Apis.get(endpoint['specialization']);
        setSpecialization(res.data);
    }

    const search = (e) => {
        e.preventDefault();
        nav(`/?doctorName=${doctorName}`)

    }

    useEffect(() => {
        loadHospitals();
        loadSpecialization();
    }, []);


    return (
        <Navbar expand="lg" className="bg-body-tertiary">
            <Container>
                <Navbar.Brand href="#home">HealCare eCommerce</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                <Nav className="me-auto">
                    
                    <Link to="/" className="nav-link" >Trang Chủ</Link>

                    <NavDropdown title="Bệnh Viện" id="basic-nav-dropdown">
                        {hospitals.map(h => <Link to={`/?hospital=${h.name}`} key={h.id} className="dropdown-item">{h.name}</Link> )}
                        
                    
                    </NavDropdown>
                    <NavDropdown title="Chuyên Khoa" id="basic-nav-dropdown">
                        {specialization.map(s => <Link to={`/?specialization=${s.name}`} key={s.id} className="dropdown-item">{s.name}</Link>)}
                    
                    </NavDropdown>
                </Nav>
                <Form inline onSubmit={search}>
                    <Row>
                    <Col xs="auto">
                        <Form.Control
                        type="text" value={doctorName} onChange={e => setDoctorName(e.target.value)}
                        placeholder="Tìm sản phẩm"
                        className=" mr-sm-2"
                        />
                    </Col>
                    <Col xs="auto">
                        <Button type="submit">Submit</Button>
                    </Col>
                    </Row>
                </Form>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default Header;