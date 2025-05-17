import axios from "axios";
import cookie from 'react-cookies'

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoints = {
    'hospitals': '/hospitals',
    'specialization': '/specialization',
    'doctors': '/doctors',
    'appointments': '/appointments',
    'register': '/users',
    'login': '/login',
    'current-user': '/secure/profile',
    'appointmentsFilter': '/appointments/filter',
    'cancelAppointment': (id) => `/secure/appointments/${id}/cancel`,
    'rescheduleAppointment': (id) => `/secure/appointments/${id}/reschedule`,
    'patientProfile': '/secure/patient/profile',
    'patientAvatar': '/secure/patient/avatar',
    'changePassword': '/secure/patient/change-password',
    'createPayment': (appointmentId) => `/secure/payment/${appointmentId}/create`,
    'processPayment': (paymentId) => `/secure/payment/${paymentId}/process`,
    'paymentReturn': () => '/payment/return',
    'payment': '/payment',
    'createPatientSelfReport': '/secure/patient-self-report/add',
    'updatePatientSelfReport': '/secure/patient-self-report',
    

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