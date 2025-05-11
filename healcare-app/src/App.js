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
                      {/* <Route path="/profile" element={<Profile />} /> */}
                      <Route path="/doctors/:id" element={<DoctorDetail />} />
                      <Route path="/appointments/new" element={<AppointmentForm />} />
                  </Routes>
              </Container>

              <Footer />
          </BrowserRouter>
      </MyUserProvider>
  );
}

export default App;
