import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Container, Row, Col, Form, Button, Card, Alert, Spinner } from 'react-bootstrap';
import Chart from 'chart.js/auto';
import { authApis, endpoints } from '../configs/Apis';
import { useMyUser } from '../configs/MyContexts';

const Statistic = () => {
    const { user } = useMyUser();
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    
    const [reportType, setReportType] = useState('patients');
    const [viewType, setViewType] = useState('dateRange'); // 'dateRange', 'month', 'quarter'

    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [year, setYear] = useState(new Date().getFullYear());
    const [month, setMonth] = useState(new Date().getMonth() + 1);
    const [quarter, setQuarter] = useState(Math.floor((new Date().getMonth() + 3) / 3));

    const [patientCount, setPatientCount] = useState(null);
    const [monthlyData, setMonthlyData] = useState([]);
    const [quarterlyData, setQuarterlyData] = useState([]);

    const [diseaseData, setDiseaseData] = useState({});
    const [isLoadingDiseases, setIsLoadingDiseases] = useState(false);

    const chartRef = useRef(null);
    const chartIdRef = useRef(`chart-${Math.random().toString(36).substring(2, 9)}`);
    const [chartInstance, setChartInstance] = useState(null);

    useEffect(() => {
        if (user && user.role !== 'DOCTOR') {
            setError('Chỉ bác sĩ mới có thể xem thống kê này');
        }
    }, [user]);

    useEffect(() => {
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

        setFromDate(firstDayOfMonth.toISOString().split('T')[0]);
        setToDate(today.toISOString().split('T')[0]);

        return () => {
            if (chartInstance) {
                chartInstance.destroy();
            }
        };
    }, []);

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
            renderDateRangeChart(response.data);
        } catch (error) {
            console.error('Error loading date range data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu.');
        } finally {
            setLoading(false);
        }
    };

    const loadMonthlyData = async () => {
        if (loading) return;
        try {
            setLoading(true);
            const data = [];
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
            renderMonthlyChart(data);
        } catch (error) {
            console.error('Error loading monthly data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu theo tháng.');
        } finally {
            setLoading(false);
        }
    };

    const loadQuarterlyData = async () => {
        if (loading) return;
        try {
            setLoading(true);
            const data = [];
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
            renderQuarterlyChart(data);
        } catch (error) {
            console.error('Error loading quarterly data:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu theo quý.');
        } finally {
            setLoading(false);
        }
    };

    const renderDateRangeChart = (count) => {
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');

        const newChart = new Chart(ctx, {
            id: chartIdRef.current,
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

    const renderMonthlyChart = (data) => {
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
            id: chartIdRef.current,
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

    const renderQuarterlyChart = (data) => {
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');

        const quarters = ['Quý 1', 'Quý 2', 'Quý 3', 'Quý 4'];

        const newChart = new Chart(ctx, {
            id: chartIdRef.current,
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


    const loadDiseasesByMonth = async () => {
        if (isLoadingDiseases) return;
        try {
            setIsLoadingDiseases(true);
            const response = await authApis().get(endpoints['doctorStatistics'] + '/disease-type-by-month', {
                params: { year }
            });
            setDiseaseData(response.data);
            renderDiseaseChart(response.data, 'bar');
        } catch (error) {
            console.error('Error loading diseases by month:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu loại bệnh theo tháng.');
        } finally {
            setIsLoadingDiseases(false);
        }
    };

    const loadDiseasesByQuarter = async () => {
        if (isLoadingDiseases) return;
        try {
            setIsLoadingDiseases(true);
            const response = await authApis().get(endpoints['doctorStatistics'] + '/disease-type-by-quarter', {
                params: { year }
            });
            setDiseaseData(response.data);
            renderDiseaseChart(response.data, 'pie');
        } catch (error) {
            console.error('Error loading diseases by quarter:', error);
            setError(error.response?.data || 'Không thể tải dữ liệu loại bệnh theo quý.');
        } finally {
            setIsLoadingDiseases(false);
        }
    };

    const renderDiseaseChart = (data, chartType) => {
        if (chartInstance) {
            chartInstance.destroy();
        }

        if (!chartRef.current) return;

        const ctx = chartRef.current.getContext('2d');
        
        const sortedData = Object.entries(data)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 10);
            
        const labels = sortedData.map(item => item[0]);
        const values = sortedData.map(item => item[1]);
        
        const backgroundColors = [
            'rgba(255, 99, 132, 0.7)',
            'rgba(54, 162, 235, 0.7)',
            'rgba(255, 206, 86, 0.7)',
            'rgba(75, 192, 192, 0.7)',
            'rgba(153, 102, 255, 0.7)',
            'rgba(255, 159, 64, 0.7)',
            'rgba(199, 199, 199, 0.7)',
            'rgba(83, 102, 255, 0.7)',
            'rgba(78, 205, 196, 0.7)',
            'rgba(247, 159, 31, 0.7)'
        ];
        
        const borderColors = backgroundColors.map(color => color.replace('0.7', '1'));
        
        const titleText = chartType === 'bar' 
            ? `Top loại bệnh phổ biến nhất năm ${year} (theo tháng)` 
            : `Top loại bệnh phổ biến nhất năm ${year} (theo quý)`;
        
        const config = {
            type: chartType,
            data: {
                labels: labels,
                datasets: [{
                    label: titleText,
                    data: values,
                    backgroundColor: backgroundColors,
                    borderColor: borderColors,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: titleText,
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: chartType === 'pie' ? 'right' : 'top',
                        display: chartType === 'pie'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.raw;
                                if (chartType === 'pie') {
                                    const total = values.reduce((sum, v) => sum + v, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${context.label}: ${value} ca (${percentage}%)`;
                                } else {
                                    return `Số ca: ${value}`;
                                }
                            }
                        }
                    }
                },
                indexAxis: chartType === 'bar' ? 'y' : undefined, // Thanh ngang cho biểu đồ bar
                scales: chartType === 'bar' ? {
                    x: {
                        beginAtZero: true,
                        ticks: { precision: 0 },
                        title: { display: true, text: 'Số ca' }
                    },
                    y: {
                        title: { display: true, text: 'Loại bệnh' }
                    }
                } : undefined
            }
        };
        
        const newChart = new Chart(ctx, config);
        setChartInstance(newChart);
    };

    const handleDateRangeSubmit = (e) => {
        e.preventDefault();
        loadDateRangeData();
    };

    const handleYearChange = (e) => {
        const newYear = parseInt(e.target.value);
        setYear(newYear);
    };

    const handleViewTypeChange = (e) => {
        setViewType(e.target.value);
        if (chartInstance) {
            chartInstance.destroy();
            setChartInstance(null);
        }
        
        setPatientCount(null);
        setMonthlyData([]);
        setQuarterlyData([]);
        setDiseaseData({});
    };

    const handleReportTypeChange = (e) => {
        const newType = e.target.value;
        setReportType(newType);
        
        if (chartInstance) {
            chartInstance.destroy();
            setChartInstance(null);
        }
        
        setPatientCount(null);
        setMonthlyData([]);
        setQuarterlyData([]);
        setDiseaseData({});
        
        setViewType('dateRange');
    };

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
            <h2 className="mb-4 text-center">Thống Kê Bác Sĩ</h2>

            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible>
                    {error}
                </Alert>
            )}

            <Card className="mb-4">
                <Card.Body>
                    <Form.Group className="mb-4">
                        <Form.Label>Chọn loại báo cáo</Form.Label>
                        <Form.Select
                            value={reportType}
                            onChange={handleReportTypeChange}
                            disabled={loading || isLoadingDiseases}
                        >
                            <option value="patients">Thống kê số lượng bệnh nhân</option>
                            <option value="diseases">Thống kê loại bệnh phổ biến</option>
                        </Form.Select>
                    </Form.Group>

                    {reportType === 'patients' && (
                        <>
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

                        </>
                    )}

                    {reportType === 'diseases' && (
                        <>
                            <Form.Group className="mb-4">
                                <Form.Label>Chọn loại thống kê</Form.Label>
                                <Form.Select
                                    value={viewType}
                                    onChange={handleViewTypeChange}
                                    disabled={isLoadingDiseases}
                                >
                                    <option value="month">Thống kê theo tháng</option>
                                    <option value="quarter">Thống kê theo quý</option>
                                </Form.Select>
                            </Form.Group>

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
                                            disabled={isLoadingDiseases}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={6} className="d-flex align-items-end">
                                    <Button
                                        onClick={viewType === 'month' ? loadDiseasesByMonth : loadDiseasesByQuarter}
                                        variant="primary"
                                        className="w-100"
                                        disabled={isLoadingDiseases}
                                    >
                                        {isLoadingDiseases ? <Spinner size="sm" animation="border" /> : 'Cập nhật biểu đồ'}
                                    </Button>
                                </Col>
                            </Row>
                        </>
                    )}

                    <div style={{ height: '400px', position: 'relative' }}>
                        {(loading || isLoadingDiseases) && (
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

                    {reportType === 'diseases' && Object.keys(diseaseData).length > 0 && (
                        <div className="mt-4">
                            <h4>Bảng chi tiết loại bệnh</h4>
                            <div className="table-responsive">
                                <table className="table table-striped table-hover">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Loại bệnh</th>
                                            <th>Số ca</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.entries(diseaseData)
                                            .sort((a, b) => b[1] - a[1])
                                            .map(([disease, count], index) => (
                                                <tr key={disease}>
                                                    <td>{index + 1}</td>
                                                    <td>{disease}</td>
                                                    <td>{count}</td>
                                                </tr>
                                            ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}

                    {reportType === 'patients' && viewType === 'month' && monthlyData.length > 0 && (
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

                    {reportType === 'patients' && viewType === 'quarter' && quarterlyData.length > 0 && (
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