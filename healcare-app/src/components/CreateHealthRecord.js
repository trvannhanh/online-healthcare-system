import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button, Form, Alert, Container, Card } from "react-bootstrap";
import { authApis, endpoints } from "../configs/Apis";

const CreateHealthRecord = () => {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    medicalHistory: "",
    examinationResults: "",
    diseaseType: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
        console.log(appointmentId);
      const url = endpoints["createHealthRecord"](appointmentId);
      const response = await authApis().post(url, formData);

      console.log("Health record created:", response.data);
      setSuccess("Kết quả khám đã được tạo thành công!");
      setTimeout(() => navigate("/appointment"), 2000);
    } catch (err) {
      console.error("Error creating health record:", err);
      setError(
        err.response?.data?.message || "Đã xảy ra lỗi khi tạo kết quả khám"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-5">
      <Card>
        <Card.Header>
          <h3>Tạo Kết Quả Khám (Lịch hẹn #{appointmentId})</h3>
        </Card.Header>
        <Card.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          {success && <Alert variant="success">{success}</Alert>}
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Tiền sử bệnh</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="medicalHistory"
                value={formData.medicalHistory}
                onChange={handleChange}
                required
                placeholder="Nhập tiền sử bệnh của bệnh nhân"
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Kết quả khám</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="examinationResults"
                value={formData.examinationResults}
                onChange={handleChange}
                required
                placeholder="Nhập kết quả khám"
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Loại bệnh</Form.Label>
              <Form.Control
                type="text"
                name="diseaseType"
                value={formData.diseaseType}
                onChange={handleChange}
                required
                placeholder="Nhập loại bệnh "
              />
            </Form.Group>
            <Button
              variant="primary"
              type="submit"
              disabled={loading}
              className="rounded-pill px-4"
            >
              {loading ? "Đang gửi..." : "Tạo Kết Quả Khám"}
            </Button>
            <Button
              variant="secondary"
              className="rounded-pill px-4 ms-2"
              onClick={() => navigate("/appointments")}
            >
              Hủy
            </Button>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default CreateHealthRecord;