import { Container } from "react-bootstrap";
import { FaHospital } from "react-icons/fa";

const Footer = () => {
    return (
        <div 
            className="py-4 text-white text-center shadow-sm" 
            style={{ 
                background: 'linear-gradient(to right, #0d6efd, #17a2b8)', 
                fontSize: '1.1rem',
                fontWeight: '500'
            }}
        >
            <Container className="d-flex flex-column flex-md-row justify-content-between align-items-center">
                <div className="d-flex align-items-center mb-2 mb-md-0">
                    <FaHospital 
                        size={30} 
                        className="me-2" 
                        style={{ transition: 'transform 0.3s' }} 
                        onMouseEnter={(e) => e.target.style.transform = 'scale(1.1)'}
                        onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                    />
                    <span>HealCare eCommerce © 2025</span>
                </div>
                <div className="d-flex gap-3">
                    <a 
                        href="/about" 
                        className="text-white text-decoration-none"
                        style={{ transition: 'color 0.2s' }}
                        onMouseEnter={(e) => e.target.style.color = '#e0f7fa'}
                        onMouseLeave={(e) => e.target.style.color = '#fff'}
                    >
                        Giới Thiệu
                    </a>
                    <a 
                        href="/contact" 
                        className="text-white text-decoration-none"
                        style={{ transition: 'color 0.2s' }}
                        onMouseEnter={(e) => e.target.style.color = '#e0f7fa'}
                        onMouseLeave={(e) => e.target.style.color = '#fff'}
                    >
                        Liên Hệ
                    </a>
                    <a 
                        href="/policy" 
                        className="text-white text-decoration-none"
                        style={{ transition: 'color 0.2s' }}
                        onMouseEnter={(e) => e.target.style.color = '#e0f7fa'}
                        onMouseLeave={(e) => e.target.style.color = '#fff'}
                    >
                        Chính Sách
                    </a>
                </div>
            </Container>
        </div>
    );
};

export default Footer;