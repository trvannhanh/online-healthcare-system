import axios from "axios";
import cookie from 'react-cookies'

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoints = {
    //USER
    'register': '/users', 
    'login': '/login', 
    'current-user': '/secure/profile',
    'chat': '/secure/chat',




    //APPOINTMENT
    'rescheduleAppointment': (id) => `/secure/appointments/${id}/reschedule`, 
    'appointments': '/appointments', 
    'bookAppointment': 'secure/appointments',
    'appointmentsFilter': '/appointments/filter', 
    'cancelAppointment': (id) => `/secure/appointments/${id}/cancel`,
    'confirmAppointment': (id) => `/secure/appointments/${id}/confirm`, 
    'createHealthRecord': (id) => `/secure/appointments/${id}/health-record`,
    'appointmentDetail': (id) => `/appointments/detail/${id}`, 


    //DOCTOR
    'createPayment': (appointmentId) => `/secure/payment/${appointmentId}/create`, 
    'hospitals': '/hospitals', //Danh sách Bệnh Viện
    'specialization': '/specialization', // Danh sách Chuyên Khoa
    'doctors': '/doctors',


    //PATIENT
    'processPayment': (paymentId) => `/secure/payment/${paymentId}/process`, 
    'paymentReturn': () => '/payment/return', 
    'payment': '/payment',
    'patients': '/patients',


    // STATISTICS
    'doctorStatistics': '/secure/statistics', 
    'diseaseByMonth': '/secure/statistics/disease-type-by-month',
    'diseaseByQuarter': '/secure/statistics/disease-type-by-quarter',

    // RATING
    'doctorAverageRating': (doctorId) => `/average/${doctorId}`,
    'ratingById': (id) => `/rating/${id}`, 
    'addRating': '/secure/patient/rating',
    'isAppointmentRated': (appointmentId) => `/rating/appointment/${appointmentId}`,
    'updateRating': (id) => `/secure/patient/rating/${id}`,
    'ratingForDoctor': (doctorId) => `/rating/doctor/${doctorId}`,
    'appointmentRating': (appointmentId) => `/rating/by-appointment/${appointmentId}`,

    //RESPONSE
    'responseById': (id) => `/response/${id}`,  
    'addResponse': '/secure/response',         
    'updateResponse': (id) => `/secure/response/${id}`, 
    'doctorRatings': (doctorId) => `/secure/doctor/ratings/${doctorId}`,  
    'responseByRating': (ratingId) => `/response/by-rating/${ratingId}`, 
    'isRatingResponsed': (ratingId) => `/response/rating/${ratingId}`,


    //SELF REPORT
    'patientProfile': '/secure/patient/profile',
    'createPatientSelfReport': '/secure/patient-self-report/add',
    'updatePatientSelfReport': '/secure/patient-self-report',
    'doctorProfile': '/secure/doctor/profile',
    'userAvatar': '/secure/avatar',
    'changePassword': '/secure/change-password',
    'patientHealthRecord': (patientId) => `/secure/patient-self-report/${patientId}`,


    //NOTIFICATION
    'notificationById': (id) => `/notification/${id}`,
    'allNotifications': '/secure/notifications/all',
    'upcomingNotifications': '/secure/notifications/upcoming',
    'markNotificationAsRead': (id) => `/secure/notifications/${id}/mark-read`,

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