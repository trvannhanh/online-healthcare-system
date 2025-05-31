import { BrowserRouter, Route, Routes } from "react-router-dom";
import Home from "./components/Home";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import { Container } from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';
import Register from "./components/Register";
import Login from "./components/Login";
import { MyUserProvider } from "./configs/MyContexts";
import DoctorDetail from "./components/DoctorDetail";
import AppointmentForm from "./components/AppointmentForm";
import Profile from "./components/Profile";
import Payment from "./components/Payment";
import ChatRoom from "./components/ChatRoom";
import Appointment from "./components/Appointment";
import Statistic from './components/Statistic';
import PendingRating from './components/PendingRating';
import Rating from './components/Rating';
import DoctorRatings from "./components/DoctorRatings";
import Response from "./components/Response";
import CreateHealthRecord from "./components/CreateHealthRecord";

const App = () => {

    return (
        <MyUserProvider>
            <BrowserRouter>
                <Header />

                <Container>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/register" element={<Register />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/profile" element={<Profile />} />
                        <Route path="/doctors/:id" element={<DoctorDetail />} />
                        <Route path="/appointment" element={<Appointment />} />
                        <Route path="/appointments/new" element={<AppointmentForm />} />
                        <Route path="/payment/:appointmentId" element={<Payment />} />
                        <Route path="/chat/:otherUserId" element={<ChatRoom />} />
                        <Route path="/doctor/statistic" element={<Statistic />} />
                        <Route path="/health-record/create/:appointmentId" element={<CreateHealthRecord />} />
                        <Route path="/pending-rating" element={<PendingRating />} />
                        <Route path="/rate-doctor/:appointmentId" element={<Rating />} />
                        <Route path="/doctor/ratings" element={<DoctorRatings />} />
                        <Route path="/doctor/response/:ratingId" element={<Response />} />
                    </Routes>
                </Container>

                <Footer />
            </BrowserRouter>
        </MyUserProvider>
    );
}


export default App;
