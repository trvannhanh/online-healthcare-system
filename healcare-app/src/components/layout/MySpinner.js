import { Spinner, Container } from "react-bootstrap";

const MySpinner = () => {
    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100px' }}>
            <Spinner 
                animation="border" 
                variant="primary" 
                style={{ 
                    width: '3rem', 
                    height: '3rem',
                    animation: 'pulse 1.5s ease-in-out infinite'
                }}
            />
            <style jsx>{`
                @keyframes pulse {
                    0% { transform: scale(1); opacity: 1; }
                    50% { transform: scale(1.2); opacity: 0.7; }
                    100% { transform: scale(1); opacity: 1; }
                }
            `}</style>
        </Container>
    );
};

export default MySpinner;