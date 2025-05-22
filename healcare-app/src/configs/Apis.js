import axios from "axios";
import cookie from 'react-cookies'

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoints = {
    'hospitals': '/hospitals',
    'specialization': '/specialization',
    'doctors': '/doctors',
    'patients': '/patients',
    'appointments': '/appointments',
    'register': '/users',
    'login': '/login',
    'current-user': '/secure/profile',
    'appointmentsFilter': '/appointments/filter',
    'cancelAppointment': (id) => `/secure/appointments/${id}/cancel`,
    'rescheduleAppointment': (id) => `/secure/appointments/${id}/reschedule`,
    'patientProfile': '/secure/patient/profile',
    'createPayment': (appointmentId) => `/secure/payment/${appointmentId}/create`,
    'processPayment': (paymentId) => `/secure/payment/${paymentId}/process`,
    'paymentReturn': () => '/payment/return',
    'payment': '/payment',
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