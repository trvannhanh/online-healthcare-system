import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Container, Row, Col, Form, Button, Card, Alert, Spinner } from 'react-bootstrap';
import Chart from 'chart.js/auto';
import { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';

const Statistic = () => {
    const { user } = useMyUser();
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [viewType, setViewType] = useState('dateRange'); // 'dateRange', 'month', 'quarter'

    // Form states
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [year, setYear] = useState(new Date().getFullYear());
    const [month, setMonth] = useState(new Date().getMonth() + 1);
    const [quarter, setQuarter] = useState(Math.floor((new Date().getMonth() + 3) / 3));

    // Chart states
    const [patientCount, setPatientCount] = useState(null);
    const [monthlyData, setMonthlyData] = useState([]);
    const [quarterlyData, setQuarterlyData] = useState([]);

    // Chart ref (single ref for all charts)
    const chartRef = useRef(null);
    const chartIdRef = useRef(`chart-${Math.random().toString(36).substring(2, 9)}`); // ID cố định cho chart

    // Chart instance (single instance)
    const [chartInstance, setChartInstance] = useState(null);

    // Check if user is doctor
    useEffect(() => {
        if (user && user.role !== 'DOCTOR') {
            setError('Chỉ bác sĩ mới có thể xem thống kê này');
        }
    }, [user]);

    // Initialize current month and date values on component mount
    useEffect(() => {
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

        setFromDate(firstDayOfMonth.toISOString().split('T')[0]);
        setToDate(today.toISOString().split('T')[0]);

        // Cleanup function
        return () => {
            if (chartInstance) {
                chartInstance.destroy();
            }
        };
    }, []); // Chỉ chạy một lần khi component được mount

    // QUAN TRỌNG: Xóa useEffect tự động load dữ liệu khi viewType thay đổi
    // Thay vào đó, chỉ load dữ liệu khi người dùng nhấn nút

    // Load data by date range
    const loadDateRangeData = async () => {
        if (!fromDate || !toDate) {
            setError('Vui lòng chọn khoảng thời gian');
            return;
        }

        try {
            setLoading(true);
            const response = await authApis().get(endpoints['doctorStatistics'] + '/doctor/patients-count', {
                params: {
                    fromDate,
                    toDate
                }
            });

            setPatientCount(response.data);

            // Create or update chart
            renderDateRangeChart(response.data);
        } catch (error) {
            console.error('Error loading date range data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu.');
        } finally {
            setLoading(false);
        }
    };

    // Load data by month
    const loadMonthlyData = async () => {
        if (loading) return;
        try {
            setLoading(true);
            // Create an array to store data for all months
            const data = [];

            // Sử dụng Promise.all để tăng tốc độ tải dữ liệu 
            const promises = [];
            for (let i = 1; i <= 12; i++) {
                promises.push(authApis().get(endpoints['doctorStatistics'] + '/doctor/patients-count-by-month', {
                    params: {
                        year,
                        month: i
                    }
                }));
            }
            
            const results = await Promise.all(promises);
            results.forEach(response => {
                data.push(response.data);
            });

            setMonthlyData(data);

            // Create or update chart
            renderMonthlyChart(data);
        } catch (error) {
            console.error('Error loading monthly data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu theo tháng.');
        } finally {
            setLoading(false);
        }
    };

    // Load data by quarter
    const loadQuarterlyData = async () => {
        if (loading) return;
        try {
            setLoading(true);
            // Create an array to store data for all quarters
            const data = [];

            // Sử dụng Promise.all để tăng tốc độ tải dữ liệu
            const promises = [];
            for (let i = 1; i <= 4; i++) {
                promises.push(authApis().get(endpoints['doctorStatistics'] + '/doctor/patients-count-by-quarter', {
                    params: {
                        year,
                        quarter: i
                    }
                }));
            }
            
            const results = await Promise.all(promises);
            results.forEach(response => {
                data.push(response.data);
            });

            setQuarterlyData(data);

            // Create or update chart
            renderQuarterlyChart(data);
        } catch (error) {
            console.error('Error loading quarterly data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu theo quý.');
        } finally {
            setLoading(false);
        }
    };

    // Render date range chart
    const renderDateRangeChart = (count) => {
        // Destroy existing chart if it exists
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');

        const newChart = new Chart(ctx, {
            id: chartIdRef.current, // Sử dụng ID cố định
            type: 'bar',
            data: {
                labels: ['Số lượng bệnh nhân'],
                datasets: [{
                    label: `Bệnh nhân từ ${fromDate} đến ${toDate}`,
                    data: [count],
                    backgroundColor: 'rgba(54, 162, 235, 0.5)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: `Thống kê số lượng bệnh nhân từ ${fromDate} đến ${toDate}`,
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });

        setChartInstance(newChart);
    };

    // Render monthly chart
    const renderMonthlyChart = (data) => {
        // Destroy existing chart if it exists
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');

        const months = [
            'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4',
            'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8',
            'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
        ];

        const newChart = new Chart(ctx, {
            id: chartIdRef.current, // Sử dụng ID cố định
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: `Số lượng bệnh nhân năm ${year}`,
                    data: data,
                    backgroundColor: 'rgba(75, 192, 192, 0.5)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: `Thống kê theo tháng - Năm ${year}`,
                        font: {
                            size: 16
                        }
                    },
                    tooltip: {
                        callbacks: {
                            title: function (tooltipItems) {
                                return months[tooltipItems[0].dataIndex];
                            },
                            label: function (context) {
                                return `Số bệnh nhân: ${context.raw}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });

        setChartInstance(newChart);
    };

    // Render quarterly chart
    const renderQuarterlyChart = (data) => {
        // Destroy existing chart if it exists
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');

        const quarters = ['Quý 1', 'Quý 2', 'Quý 3', 'Quý 4'];

        const newChart = new Chart(ctx, {
            id: chartIdRef.current, // Sử dụng ID cố định
            type: 'bar',
            data: {
                labels: quarters,
                datasets: [{
                    label: `Số lượng bệnh nhân năm ${year}`,
                    data: data,
                    backgroundColor: 'rgba(153, 102, 255, 0.5)',
                    borderColor: 'rgba(153, 102, 255, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: `Thống kê theo quý - Năm ${year}`,
                        font: {
                            size: 16
                        }
                    },
                    tooltip: {
                        callbacks: {
                            title: function (tooltipItems) {
                                return quarters[tooltipItems[0].dataIndex];
                            },
                            label: function (context) {
                                return `Số bệnh nhân: ${context.raw}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });

        setChartInstance(newChart);
    };

    // Handle form submission for date range
    const handleDateRangeSubmit = (e) => {
        e.preventDefault();
        loadDateRangeData();
    };

    // Handle year change for monthly and quarterly charts
    const handleYearChange = (e) => {
        const newYear = parseInt(e.target.value);
        setYear(newYear);
        // XÓA: không tự động tải dữ liệu khi năm thay đổi
    };

    // XÓA useEffect tự động tải khi thay đổi năm
    // Thay vào đó, chỉ tải dữ liệu khi người dùng nhấn nút "Cập nhật biểu đồ"

    // Handle change of view type
    const handleViewTypeChange = (e) => {
        setViewType(e.target.value);
        // Xóa dữ liệu hiện tại khi chuyển đổi loại xem
        if (chartInstance) {
            chartInstance.destroy();
            setChartInstance(null);
        }
        
        setPatientCount(null);
        setMonthlyData([]);
        setQuarterlyData([]);
    };

    // If not a doctor, show access denied
    if (user && user.role !== 'DOCTOR') {
        return (
            <Container className="my-4">
                <Alert variant="danger">
                    Bạn không có quyền truy cập vào thống kê này. Chỉ bác sĩ mới có thể xem.
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="my-4">
            <h2 className="mb-4 text-center">Thống Kê Bệnh Nhân</h2>

            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible>
                    {error}
                </Alert>
            )}

            <Card className="mb-4">
                <Card.Body>
                    <Form.Group className="mb-4">
                        <Form.Label>Chọn loại thống kê</Form.Label>
                        <Form.Select
                            value={viewType}
                            onChange={handleViewTypeChange}
                            disabled={loading}
                        >
                            <option value="dateRange">Thống kê theo khoảng thời gian</option>
                            <option value="month">Thống kê theo tháng</option>
                            <option value="quarter">Thống kê theo quý</option>
                        </Form.Select>
                    </Form.Group>

                    {viewType === 'dateRange' && (
                        <div className="mb-4">
                            <Form onSubmit={handleDateRangeSubmit}>
                                <Row>
                                    <Col md={5}>
                                        <Form.Group>
                                            <Form.Label>Từ ngày</Form.Label>
                                            <Form.Control
                                                type="date"
                                                value={fromDate}
                                                onChange={(e) => setFromDate(e.target.value)}
                                                disabled={loading}
                                                required
                                            />
                                        </Form.Group>
                                    </Col>
                                    <Col md={5}>
                                        <Form.Group>
                                            <Form.Label>Đến ngày</Form.Label>
                                            <Form.Control
                                                type="date"
                                                value={toDate}
                                                onChange={(e) => setToDate(e.target.value)}
                                                disabled={loading}
                                                required
                                            />
                                        </Form.Group>
                                    </Col>
                                    <Col md={2} className="d-flex align-items-end">
                                        <Button
                                            type="submit"
                                            variant="primary"
                                            className="w-100"
                                            disabled={loading}
                                        >
                                            {loading ? <Spinner size="sm" animation="border" /> : 'Xem thống kê'}
                                        </Button>
                                    </Col>
                                </Row>
                            </Form>
                        </div>
                    )}

                    {(viewType === 'month' || viewType === 'quarter') && (
                        <Row className="mb-4">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Chọn năm</Form.Label>
                                    <Form.Control
                                        type="number"
                                        value={year}
                                        onChange={handleYearChange}
                                        min="2000"
                                        max="2100"
                                        disabled={loading}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6} className="d-flex align-items-end">
                                <Button
                                    onClick={viewType === 'month' ? loadMonthlyData : loadQuarterlyData}
                                    variant="primary"
                                    className="w-100"
                                    disabled={loading}
                                >
                                    {loading ? <Spinner size="sm" animation="border" /> : 'Cập nhật biểu đồ'}
                                </Button>
                            </Col>
                        </Row>
                    )}

                    {/* Phần hiển thị biểu đồ - dùng chung cho mọi loại */}
                    <div style={{ height: '400px', position: 'relative' }}>
                        {loading && (
                            <div style={{
                                position: 'absolute',
                                top: 0,
                                left: 0,
                                width: '100%',
                                height: '100%',
                                display: 'flex',
                                justifyContent: 'center',
                                alignItems: 'center',
                                backgroundColor: 'rgba(255,255,255,0.7)',
                                zIndex: 10
                            }}>
                                <Spinner animation="border" />
                            </div>
                        )}
                        <canvas ref={chartRef}></canvas>
                    </div>

                    {/* Hiển thị thống kê tổng hợp khi đang ở chế độ xem theo tháng */}
                    {viewType === 'month' && monthlyData.length > 0 && (
                        <div className="mt-4">
                            <Row>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-primary text-white">Tổng số bệnh nhân</Card.Header>
                                        <Card.Body>
                                            <h3>{monthlyData.reduce((sum, val) => sum + val, 0)}</h3>
                                        </Card.Body>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-success text-white">Cao nhất</Card.Header>
                                        <Card.Body>
                                            <h3>{monthlyData.length > 0 ? Math.max(...monthlyData) : 0}</h3>
                                            <p>
                                                {monthlyData.length > 0
                                                    ? `Tháng ${monthlyData.indexOf(Math.max(...monthlyData)) + 1}`
                                                    : '-'}
                                            </p>
                                        </Card.Body>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-info text-white">Trung bình</Card.Header>
                                        <Card.Body>
                                            <h3>
                                                {monthlyData.length > 0
                                                    ? (monthlyData.reduce((sum, val) => sum + val, 0) / monthlyData.length).toFixed(1)
                                                    : 0}
                                            </h3>
                                            <p>bệnh nhân/tháng</p>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            </Row>
                        </div>
                    )}

                    {/* Hiển thị thống kê tổng hợp khi đang ở chế độ xem theo quý */}
                    {viewType === 'quarter' && quarterlyData.length > 0 && (
                        <div className="mt-4">
                            <Row>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-primary text-white">Tổng số bệnh nhân</Card.Header>
                                        <Card.Body>
                                            <h3>{quarterlyData.reduce((sum, val) => sum + val, 0)}</h3>
                                        </Card.Body>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-success text-white">Cao nhất</Card.Header>
                                        <Card.Body>
                                            <h3>{quarterlyData.length > 0 ? Math.max(...quarterlyData) : 0}</h3>
                                            <p>
                                                {quarterlyData.length > 0
                                                    ? `Quý ${quarterlyData.indexOf(Math.max(...quarterlyData)) + 1}`
                                                    : '-'}
                                            </p>
                                        </Card.Body>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card className="text-center">
                                        <Card.Header className="bg-info text-white">Trung bình</Card.Header>
                                        <Card.Body>
                                            <h3>
                                                {quarterlyData.length > 0
                                                    ? (quarterlyData.reduce((sum, val) => sum + val, 0) / quarterlyData.length).toFixed(1)
                                                    : 0}
                                            </h3>
                                            <p>bệnh nhân/quý</p>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            </Row>
                        </div>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Statistic;