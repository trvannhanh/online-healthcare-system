import { BrowserRouter, Route, Routes } from "react-router-dom";
import Home from "./components/Home";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import { Container } from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';

const App = () => {

  return (
    <BrowserRouter>
      <Header />
  
      <Container>
        <Routes>
          <Route path="/" element={<Home />} />
        </Routes>
      </Container>
      
      <Footer />
    </BrowserRouter>
  );
}

export default App;
