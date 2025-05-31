import axios from "axios";
import cookie from 'react-cookies'

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoints = {
    //USER
    'register': '/users', //Đăng ký tài khoản
    'login': '/login', // Đăng nhập
    'current-user': '/secure/profile',
    'chat': '/secure/chat',




    //APPOINTMENT
    'rescheduleAppointment': (id) => `/secure/appointments/${id}/reschedule`, //Đổi thời gian lịch hẹn
    'appointments': '/appointments', // Danh sách lịch hẹn
    'bookAppointment': 'secure/appointments',
    'appointmentsFilter': '/appointments/filter', //Lọc danh sách lịch hẹn
    'cancelAppointment': (id) => `/secure/appointments/${id}/cancel`, //Hủy lịch hẹn
    'confirmAppointment': (id) => `/secure/appointments/${id}/confirm`, // Xác nhận lịch hẹn
    'createHealthRecord': (id) => `/secure/appointments/${id}/health-record`,
    'appointmentDetail': (id) => `/appointments/detail/${id}`, // Chi tiết lịch hẹn


    //DOCTOR
    'createPayment': (appointmentId) => `/secure/payment/${appointmentId}/create`, //Tạo hóa đơn
    'hospitals': '/hospitals', //Danh sách Bệnh Viện
    'specialization': '/specialization', // Danh sách Chuyên Khoa
    'doctors': '/doctors',


    //PATIENT
    'processPayment': (paymentId) => `/secure/payment/${paymentId}/process`, //Thanh Toán Hóa Đơn
    'paymentReturn': () => '/payment/return', //Trả về Thanh Toán
    'payment': '/payment',
    'patients': '/patients',


    // STATISTICS
    'doctorStatistics': '/secure/statistics', // Thống kê của bác sĩ
    'diseaseByMonth': '/secure/statistics/disease-type-by-month',
    'diseaseByQuarter': '/secure/statistics/disease-type-by-quarter',

    // RATING
    'doctorAverageRating': (doctorId) => `/average/${doctorId}`,
    'ratingById': (id) => `/rating/${id}`, // Lấy đánh giá theo ID
    'addRating': '/secure/patient/rating',
    'isAppointmentRated': (appointmentId) => `/rating/appointment/${appointmentId}`,
    'updateRating': (id) => `/secure/patient/rating/${id}`,
    'ratingForDoctor': (doctorId) => `/rating/doctor/${doctorId}`,
    'appointmentRating': (appointmentId) => `/rating/by-appointment/${appointmentId}`,//Lấy đánh giá theo lịch hẹn

    //RESPONSE
    'responseById': (id) => `/response/${id}`,  // Lấy phản hồi theo ID
    'addResponse': '/secure/response',           // Thêm phản hồi mới
    'updateResponse': (id) => `/secure/response/${id}`, // Cập nhật phản hồi
    'doctorRatings': (doctorId) => `/secure/doctor/ratings/${doctorId}`,  // Lấy đánh giá của bác sĩ  //  Lấy đánh giá của bác sĩ
    'responseByRating': (ratingId) => `/response/by-rating/${ratingId}`, //  Lấy phản hồi theo rating ID
    'isRatingResponsed': (ratingId) => `/response/rating/${ratingId}`,


    'patientProfile': '/secure/patient/profile',
    'createPatientSelfReport': '/secure/patient-self-report/add',
    'updatePatientSelfReport': '/secure/patient-self-report',
    'doctorProfile': '/secure/doctor/profile',
    'userAvatar': '/secure/avatar',
    'changePassword': '/secure/change-password',


    // 'healthRecord': (id) => `/secure/health-records/${id}`,
    // 'createHealthRecord': '/secure/health-records/add',
    // 'updateHealthRecord': (id) => `/secure/health-records/${id}`
}

export const authApis = () => {
    return axios.create({
        baseURL: BASE_URL,
        headers: {
            'Authorization': `Bearer ${cookie.load('token')}`
        }
    })
}

export default axios.create({
    baseURL: BASE_URL
})